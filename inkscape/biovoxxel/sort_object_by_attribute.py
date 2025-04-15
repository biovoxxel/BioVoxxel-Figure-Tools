#!/usr/bin/env python3


# Copyright (C) 2025  Jan Brocher, BioVoxxel, jan.brocher@biovoxxel.de

#   This program is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with this program.  If not, see <https://www.gnu.org/licenses/>.


#!/usr/bin/env python3
import inkex

class SortSelectedObjects(inkex.EffectExtension):
    def add_arguments(self, pars):
        # Dropdown to choose sorting method
        pars.add_argument("--sort_by", type=str, default="label", help="Sort objects by: label, id, or title")

    def effect(self):
        selection = list(self.svg.selection.values())
        if not selection:
            inkex.errormsg("No objects selected.")
            return

        # inkex.errormsg(f"Sorting {len(selection)} selected objects by {self.options.sort_by}.")

        # Group selected elements by parent
        parent_groups = {}
        for elem in selection:
            parent = elem.getparent()
            if parent is None:
                continue
            parent_groups.setdefault(parent, []).append(elem)

        # Process each parent group
        for parent, elems in parent_groups.items():
            parent_id = parent.get("id", "unknown")

            # Get sorting key based on user's choice
            def get_sort_key(e):
                if self.options.sort_by == "label":
                    return e.get('{http://www.inkscape.org/namespaces/inkscape}label', '').lower()
                elif self.options.sort_by == "title":
                    return e.get('{http://www.w3.org/2000/svg}title', '').lower()
                else:  # Default to sorting by ID
                    return e.get_id().lower()

            sorted_elems = sorted(elems, key=get_sort_key)

            # Remove and reinsert sorted elements in order
            indices = [list(parent).index(e) for e in elems]
            insert_index = min(indices) if indices else len(parent)

            for elem in elems:
                parent.remove(elem)
            for i, elem in enumerate(sorted_elems):
                parent.insert(insert_index + i, elem)

            # inkex.errormsg(f"Sorted objects in parent '{parent_id}'.")

if __name__ == '__main__':
    SortSelectedObjects().run()
