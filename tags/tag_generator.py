
# Main function for when the file is run directly
from io import TextIOWrapper
import regex
import os
import json

# Read file and return all lines
def readFile(filepath: str) -> list[str]:
    file : TextIOWrapper = open(filepath)
    strings : list[str] = []
    for line in file:
        # Filter out # and empty lines
        if line.startswith("#") or line == "":
            continue;
        strings.append(line.strip())
    return strings;

# A tag or tag group, representing either a standalone entry or a list in the md file
class Tag:
    def __init__(self, name):
        self._members : list[Tag] = []
        self._name : str = name

    def add_member(self, other : "Tag") -> None :
        self._members.append(other)

    def is_group(self) -> bool :
        return len(self._members) > 0
    
    def get_members(self) -> list["Tag"] :
        return self._members
    
    def get_name(self) -> str :
        return self._name

def parse_tags(strings) -> list[Tag] :
    tags : list[Tag] = []

    # limit tag names to only a-z and _
    pattern = regex.compile("[^a-z_]+")
    
    is_list_item : bool = False

    for line in strings:
        # lower case
        tag_str : str = line.lower()
        # list item - part of a tag group
        if line.startswith("-"):
            tag_str = tag_str[1:]
            tag_str = tag_str.strip()
            is_list_item = True
        # not a list item - standalone, or tag group
        else:
            is_list_item = False

        # no spaces
        tag_str.replace(" ", "_")

        # error if name doesn't match the pattern
        if regex.match(pattern, tag_str):
            print("Incorrect formatting of tag: "+tag_str)
            continue

        if len(tag_str) == 0:
            continue

        # make the tag object
        tag : Tag = Tag(tag_str)

        # if part of tag group, add it to it
        if is_list_item:
            group : Tag = tags[-1]
            group.add_member(tag)
        # add to the list of tags/tag groups
        else:
            tags.append(tag)
    return tags

def make_tag_file(folder : str, tag_path : str, tag : Tag) :
    # make sure folder contains separator
    if not folder.endswith(os.sep):
        folder += os.sep
    
    # make sure tag path includes separator
    if len(tag_path) > 0 and not tag_path.endswith(os.sep):
        tag_path += os.sep

    # replace path separator with / for tag scope
    tag_path_json = tag_path.replace(os.sep, "/")

    # open json file for writing (not appending), and creates if it doesn't exist
    out_file = open(folder + tag_path + tag.get_name() + ".json", "w+")

    # defining the json using a dictionary
    out_json = {
        'values': [
            "#c:"+tag_path_json+tag.get_name()
        ]
    }

    # convert the json dict to a printable string
    out_str = json.dumps(out_json)

    # write the json to file
    out_file.write(out_str)

    # close the file
    out_file.close()

if __name__ == "__main__":
    # Read file "list_of_tags.md"
    strings : list[str] = readFile("list_of_tags.md")
    tags : list[Tag] = parse_tags(strings)

    folder = "out" + os.sep

    os.makedirs(folder, exist_ok=True)

    for tag in tags:
        if tag.is_group():
            os.makedirs(folder + tag.get_name(), exist_ok=True)
            for child in tag.get_members():
                make_tag_file(folder, tag.get_name(), child)
        else:
            make_tag_file(folder, "", tag)
        

        
        



    