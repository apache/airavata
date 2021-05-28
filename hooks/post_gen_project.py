import os
import sys


def find_setup_cfg_file():
    currdir = os.getcwd()
    while currdir != os.path.dirname(currdir):
        setupcfg_path = os.path.join(currdir, "setup.cfg")
        if os.path.isfile(setupcfg_path):
            return setupcfg_path
        else:
            currdir = os.path.dirname(currdir)
    # Couldn't find it, returning None
    return None

def find_last_line_of_output_view_providers(setup_cfg_lines):
    output_vps_start = -1
    output_vps_end = -1
    for index, line in enumerate(setup_cfg_lines):
        if line.strip().startswith('airavata.output_view_providers'):
            output_vps_start = index
        # If the line is non-blank and has a dangling item, update the end index
        elif line.startswith(' ') and line.strip() != '' and output_vps_start > 0:
            output_vps_end = index
        elif output_vps_end > 0:
            break
    # Return None if we couldn't find it
    return output_vps_end if output_vps_end > 0 else None

def find_last_line_of_entry_points(setup_cfg_lines):
    entry_points_start = -1
    entry_points_end = -1
    for index, line in enumerate(setup_cfg_lines):
        if line.strip() == '[options.entry_points]':
            entry_points_start = index
        # If the line is non-blank and doesn't start a new section, update the end index
        elif line.strip() != '' and not line.startswith('[') and entry_points_start > 0:
            entry_points_end = index
        elif entry_points_end > 0:
            break
    # Return None if we couldn't find it
    return entry_points_end if entry_points_end > 0 else None

def insert_output_view_provider(setup_cfg_lines, index, insert_entry_point_group=False):
    updated_lines = setup_cfg_lines.copy()
    updated_lines.insert(index+1, "    {{cookiecutter.project_slug}} = FIXME.{{cookiecutter.output_views_directory_name}}:{{cookiecutter.output_view_provider_class_name}}" + os.linesep)
    if insert_entry_point_group:
        updated_lines.insert(index+1, "airavata.output_view_providers =" + os.linesep)
    return updated_lines

setup_cfg_file = find_setup_cfg_file()
if not setup_cfg_file:
    print("Could not find setup.cfg file! Are you running this from within a custom Django app?", file=sys.stderr)
    sys.exit(1)

with open(setup_cfg_file, 'r+') as f:
    lines = f.readlines()
    end_of_output_view_providers = find_last_line_of_output_view_providers(lines)
    updated_lines = None
    if end_of_output_view_providers is not None:
        updated_lines = insert_output_view_provider(lines, end_of_output_view_providers)
    else:
        end_of_entry_points = find_last_line_of_entry_points(lines)
        if end_of_entry_points is not None:
            updated_lines = insert_output_view_provider(lines, end_of_entry_points, insert_entry_point_group=True)
    if updated_lines is None:
        print("Could not find insertion point for output view provider entry point!", file=sys.stderr)
        sys.exit(1)
    else:
        f.seek(0)
        f.writelines(updated_lines)

