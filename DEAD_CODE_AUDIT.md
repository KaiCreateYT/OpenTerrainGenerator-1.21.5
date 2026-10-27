# Audyt martwego kodu i śmieci w OpenTerrainGenerator

Data: 2026-02-16

---

## KRYTYCZNE — aktywne bugi ukryte w martwym kodzie

### ~~1. `Constants.WORLD_DEPTH=0` / `WORLD_HEIGHT=256` — deprecated ale używane w ~40 miejscach~~ NAPRAWIONE

**Status: NAPRAWIONE** (2026-02-16)

Deprecated stale `WORLD_DEPTH` i `WORLD_HEIGHT` zostaly usuniete z `Constants.java`. Wszystkie call sites zamienione na:
- `worldGenRegion.getWorldInfo().minY()` / `.maxY()` — tam gdzie `IWorldGenRegion` jest dostepne (BO2, BO3, BO4, BlockCheck, FrozenSurfaceHelper, BO4CustomStructure)
- `Constants.WORLD_START_MIN_Y` / `WORLD_END_MAX_Y` — w statycznych definicjach settings (BO2Settings, BO3Settings, BO4Settings, SurfaceSettings, CarverSettings)
- `BO3Enums.ExtrudeMode` — usuniety hardcoded Y z enuma, `ObjectExtrusionHelper` otrzymuje `OTGWorldInfo` dynamicznie
- `BO3.getOffsetAndVariance()` — dodany parametr `IWorldGenRegion` do clamp na prawidlowe world bounds

### 2. `throw new RuntimeException()` — 4 bomby w produkcyjnym kodzie

`BO4CustomStructure.java` linie **626, 631, 665, 1079**:
```java
throw new RuntimeException(); // TODO: Remove after testing
```
Runtime exceptions "do testów" nigdy nieusunięte. Jeden zły branch w strukturze BO4 i crash.

### 3. `MinecraftObjectFunction.spawn()` — kompletnie niezaimplementowane

Obie metody `spawn()` mają puste body z zakomentowanym kodem. Funkcja `MinecraftObject(x,y,z,name)` w plikach BO3/BO4 **nic nie robi**. Cicha awaria.

### 4. Martwe bloki event-checkowe w BO2/BO3

`BO2.java:350-355` i `BO3.java:411-416`:
```java
//if (!worldGenRegion.fireCanCustomObjectSpawnEvent(this, x, y, z))
{
    //return false;
}
```
Zakomentowany `if` ale **klamry zostały** — pusty blok, event nigdy nie odpala, obiekty zawsze się spawnują niezależnie od warunków.

### 5. Rzeki nie generują się

Trzy osobne `TODO: This generates no rivers atm` w `BiomeLayers.java` (linie 136, 148, 202). Feature jest martwy.

---

## WYSOKIE — poważne śmieci

### 6. `e.printStackTrace()` zamiast loggera — 50+ wywołań

Największy sprawca: `CustomStructureFileManager.java` — **24 bare `printStackTrace()`**. Dalej:
- `BO4Config.java` — 10 wywołań
- `DimensionConfigLoader.java` — 4 wywołania
- `EntityFunction.java`, `ObjectCreator.java`, `Extractor.java`, `MaterialSetting.java`, `MaterialListSetting.java`, `ConfigSection.java`, `SpawnCommand.java`, itd.

Wszystkie omijają OTGLog, lecą na stderr bez formatowania ani filtrowania.

### 7. Puste catch bloki połykające wyjątki

`CustomStructureFileManager.java` — **9x `catch (Exception ignored) {}`** — korupcja danych strukturalnych jest cicho ignorowana. Także:
- `OTGWorldStorage.java` — corrupt file handling (now with backup + fallback)
- `DimensionManager.java:73` — `catch (Exception ignored) {}`
- `BO4BranchFunction.java:142`, `BO4WeightedBranchFunction.java:67`

### 8. Masowe duplikaty kodu Fabric / NeoForge

| Plik Fabric | Plik NeoForge | Linie |
|---|---|---|
| `FabricWorldGenRegion.java` | `NeoForgeWorldGenRegion.java` | ~858 vs ~858, niemal identyczne |
| `LegacyFabricBiomeLoader.java` | `LegacyNeoForgeBiomeLoader.java` | ~885 vs ~874, ten sam algorytm |
| `OTGFabricChunkGenerator.java` | `OTGNeoForgeChunkGenerator.java` | ~734 vs ~774, duży overlap |
| `RegistryLoaderMixin` (fabric) | `RegistryLoaderMixin` (neoforge) | identyczny debug code |

Powinno to siedzieć w `platforms/shared/`.

---

## ŚREDNIE — martwy kod do usunięcia

### 9. Martwe interfejsy i klasy abstrakcyjne

| Plik | Problem |
|---|---|
| `IBiomeRegistryProvider.java` | Zero implementacji, zero referencji |
| `INoiseSampler.java` | Pusty interfejs, zero implementacji, zero referencji |
| `BiomeGroupLoader.java` | Abstrakcyjna klasa, zero podklas, zero importów |
| `BiomeLoader.java` | Abstrakcyjna klasa, 3 metody, zero podklas |

### 10. Martwe enumy i wartości

| Enum | Problem |
|---|---|
| `EntityCategory` (cały enum) | Wszystkie 6 wartości używane tylko w zakomentowanym kodzie Forge |
| `OTGDimensionType.OVERWORLD/NETHER/END` | Tylko `.OTG` jest gdziekolwiek używane |
| `OTGDirection.DOWN` | Nigdy niereferencjonowane |

### 11. Nieużywane stałe

- `Constants.ChannelName` — nigdy niereferencjonowane
- `Constants.ProtocolVersion` — nigdy niereferencjonowane

### 12. Martwe metody w `Preset.java`

- `getBiomeTemplate(String)` — zero call sites
- `getBiomeID(int)` — zero call sites
- `getBiomeIDByRegistryName(String)` — zero call sites
- `getBiomeColorMap()` — getter z Lomboka, nigdy niewywoływany

### 13. Martwe ustawienia konfiguracji

15+ ustawień struktur (`STRONGHOLDS_ENABLED`, `OCEAN_MONUMENTS_ENABLED`, `VILLAGE_TYPE`, itp.) w `BiomeStructureSettings.java` — parsowane i przechowywane, ale writer ma je **zakomentowane** w `BiomeConfigWriter.java:421-486`. Na Fabric nie robią nic — tag-based system w `BiomeStructureTagConfig` robi robotę.

---

## NISKIE — śmieci kosmetyczne

### 14. `System.out.println` w produkcyjnym kodzie

| Plik | Problem |
|---|---|
| `CrashDummy.java` | Cała klasa istnieje po to żeby printować na konstrukcji |
| `NamedBinaryTag.java:597-651` | Metoda `print()` dumpuje drzewo NBT na stdout |
| `SettingsSchemaGenerator.java:63,229,253` | Printy + copy-paste bug (mówi "schema.json" a pisze inny plik) |
| `RegistryLoaderMixin` (fabric+neoforge) | `--*--` debug markers w martwej metodzie `printAllRegistriesForDebug()` |

### 15. `OTGLog.BasicLogger` — odwrócona logika stdout/stderr

`OTGLog.java:124-126`: `level >= WARN` leci na `System.out`, reszta na `System.err`. Powinno być odwrotnie.

### 16. Komentarz "Frank's stupidity"

`BlockFunction.java:100-138` — ~35 linii zakomentowanego if-else łańcucha do mappowania doniczkowych roślin, poprzedzone:
```java
/*
 * The following commented-out code is a testemant to Frank's stupidity.
 * To anyone reading this, Frank is a complete and total idiot.
 */
```
Kod nad tym obsługuje to pętlą. Do wywalenia.

### 17. `BO3Config.isOTGPlus` — pole-zombie

Hardcoded na `false`, po wczytaniu jako `true` rzuca wyjątek, po walidacji zapisywane jako `false`. Mogłoby być lokalną zmienną.

---

## Podsumowanie liczbowe

| Kategoria | Ilość |
|---|---|
| Aktywne bugi w martwym kodzie | ~~5~~ 4 (1 naprawiony) |
| `e.printStackTrace()` | ~50 |
| Puste catch bloki | ~15 |
| Martwe klasy/interfejsy | 4 |
| Martwe metody | 5+ |
| Martwe enum wartości | ~10 |
| Nieużywane stałe | 4 |
| Zakomentowane bloki kodu | ~10 |
| `System.out.println` w prod | ~15 |
| TODO komentarzy | ~200 |
| Zduplikowany kod (Fabric/NeoForge) | ~2500 linii |
