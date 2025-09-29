#!/usr/bin/env python3

# Read the file
with open('app/src/main/java/com/delhivery/axle/ui/contractDetails/ContractDetailsActivity.kt', 'r') as f:
    content = f.read()

lines = content.split('\n')

# Find where to insert the method (after placementInput method ends)
insert_index = -1
for i, line in enumerate(lines):
    if '    })' in line and 'private fun  enableSubmitPlacement()' in lines[i+1] if i+1 < len(lines) else False:
        insert_index = i + 1
        break

if insert_index != -1:
    # Insert the method
    method_lines = [
        '',
        '  private fun showAddTruckBottomSheet() {',
        '    val dialog = AddTruckBottomSheetDialog(',
        '      this,',
        '      viewModelFactory,',
        '      userPrefs,',
        '      autoCompleteUtils,',
        '      onTruckAdded = { truckNumber ->',
        '        // Populate the vehicle number field with the newly added truck',
        '        binding.cardInput.placementCl.editTextVehicleNumber.setText(truckNumber)',
        '        binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE',
        '        binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE',
        '        isValidVehicleNumber = true',
        '        enableSubmitPlacement()',
        '      }',
        '    )',
        '    dialog.show()',
        '  }'
    ]
    
    # Insert the method lines
    for i, method_line in enumerate(method_lines):
        lines.insert(insert_index + i, method_line)

# Write the file back
with open('app/src/main/java/com/delhivery/axle/ui/contractDetails/ContractDetailsActivity.kt', 'w') as f:
    f.write('\n'.join(lines))

print("AddTruckBottomSheetDialog method added successfully!")
