#!/usr/bin/env python3

import re

# Read the file
with open('app/src/main/java/com/delhivery/axle/ui/contractDetails/ContractDetailsActivity.kt', 'r') as f:
    content = f.read()

# Add imports after line 5 (import android.content.Intent)
lines = content.split('\n')
import_line_index = -1
for i, line in enumerate(lines):
    if 'import android.content.Intent' in line:
        import_line_index = i
        break

if import_line_index != -1:
    lines.insert(import_line_index + 1, 'import android.provider.ContactsContract')
    lines.insert(import_line_index + 2, 'import android.database.Cursor')

# Add companion object after class declaration
class_line_index = -1
for i, line in enumerate(lines):
    if 'class ContractDetailsActivity: BaseActivity' in line:
        class_line_index = i
        break

if class_line_index != -1:
    lines.insert(class_line_index + 1, '')
    lines.insert(class_line_index + 2, '  companion object {')
    lines.insert(class_line_index + 3, '    private const val REQCODE_PICK_CONTACT = 2001')
    lines.insert(class_line_index + 4, '  }')

# Add contact picker functionality to placementInput method
placement_input_end = -1
for i, line in enumerate(lines):
    if 'binding.cardInput.placementCl.editTextDriverNumber?.addTextChangedListener' in line:
        # Find the end of this method
        brace_count = 0
        for j in range(i, len(lines)):
            if '{' in lines[j]:
                brace_count += lines[j].count('{')
            if '}' in lines[j]:
                brace_count -= lines[j].count('}')
            if brace_count == 0 and '}' in lines[j]:
                placement_input_end = j
                break
        break

if placement_input_end != -1:
    lines.insert(placement_input_end + 1, '')
    lines.insert(placement_input_end + 2, '    // Contact picker functionality')
    lines.insert(placement_input_end + 3, '    binding.cardInput.placementCl.btnContactPicker.setOnClickListener {')
    lines.insert(placement_input_end + 4, '        val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)')
    lines.insert(placement_input_end + 5, '        startActivityForResult(pickContactIntent, REQCODE_PICK_CONTACT)')
    lines.insert(placement_input_end + 6, '    }')

# Add onActivityResult method after bidPlacedSuccess
bid_placed_success_end = -1
for i, line in enumerate(lines):
    if 'override fun bidPlacedSuccess(success: Boolean)' in line:
        # Find the end of this method
        brace_count = 0
        for j in range(i, len(lines)):
            if '{' in lines[j]:
                brace_count += lines[j].count('{')
            if '}' in lines[j]:
                brace_count -= lines[j].count('}')
            if brace_count == 0 and '}' in lines[j]:
                bid_placed_success_end = j
                break
        break

if bid_placed_success_end != -1:
    lines.insert(bid_placed_success_end + 1, '')
    lines.insert(bid_placed_success_end + 2, '  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {')
    lines.insert(bid_placed_success_end + 3, '    super.onActivityResult(requestCode, resultCode, data)')
    lines.insert(bid_placed_success_end + 4, '    when (requestCode) {')
    lines.insert(bid_placed_success_end + 5, '      REQCODE_PICK_CONTACT -> {')
    lines.insert(bid_placed_success_end + 6, '        if (resultCode == RESULT_OK && data != null) {')
    lines.insert(bid_placed_success_end + 7, '          val contactUri: Uri? = data.data')
    lines.insert(bid_placed_success_end + 8, '          contactUri?.let {')
    lines.insert(bid_placed_success_end + 9, '            val cursor: Cursor? = contentResolver.query(it, null, null, null, null)')
    lines.insert(bid_placed_success_end + 10, '            cursor?.use { c ->')
    lines.insert(bid_placed_success_end + 11, '              if (c.moveToFirst()) {')
    lines.insert(bid_placed_success_end + 12, '                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)')
    lines.insert(bid_placed_success_end + 13, '                if (numberIndex != -1) {')
    lines.insert(bid_placed_success_end + 14, '                  val phoneNumber = c.getString(numberIndex)')
    lines.insert(bid_placed_success_end + 15, '                  val trimmedNumber = phoneNumber?.replace("+91", "")?.trim()')
    lines.insert(bid_placed_success_end + 16, '                  binding.cardInput.placementCl.editTextDriverNumber.setText(trimmedNumber)')
    lines.insert(bid_placed_success_end + 17, '                }')
    lines.insert(bid_placed_success_end + 18, '              }')
    lines.insert(bid_placed_success_end + 19, '            }')
    lines.insert(bid_placed_success_end + 20, '          }')
    lines.insert(bid_placed_success_end + 21, '        }')
    lines.insert(bid_placed_success_end + 22, '      }')
    lines.insert(bid_placed_success_end + 23, '    }')
    lines.insert(bid_placed_success_end + 24, '  }')

# Write the file back
with open('app/src/main/java/com/delhivery/axle/ui/contractDetails/ContractDetailsActivity.kt', 'w') as f:
    f.write('\n'.join(lines))

print("Contact picker functionality added successfully!")
