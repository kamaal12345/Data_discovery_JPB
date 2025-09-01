import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'deepFilter',
})
export class DeepFilterPipe implements PipeTransform {

  transform(items: any[], searchText: string): any[] {
    if (!items || !searchText) return items;  // If no items or searchText, return original list

    // Normalize the search text (trim and lowercase)
    searchText = searchText.trim().toLowerCase();

    // Filter the items array
    return items
      .map(item => this.filterItem(item, searchText))  // Filter each item
      .filter(item => item !== null);  // Remove null values (items with no matches)
  }

  /**
   * Filter a single item and return it with filtered sublists if necessary.
   * @param item The item to be filtered.
   * @param searchText The text to search for.
   * @returns The filtered item or null if no matches are found.
   */
  private filterItem(item: any, searchText: string): any {
    if (!item) return null;  // If the item is null or undefined, return null

    // Create a shallow copy to avoid modifying the original item
    const newItem = { ...item };

    // Iterate over the object's keys and filter sublist properties
    for (const key in newItem) {
      if (Array.isArray(newItem[key])) {
        // If the value is an array (sublist), filter the array
        newItem[key] = this.filterSublist(newItem[key], searchText);
      } else if (typeof newItem[key] === 'object') {
        // If the value is an object, recursively filter it
        newItem[key] = this.filterItem(newItem[key], searchText);
      }
    }

    // If the item has any matches in its relevant fields, return it
    return this.hasMatch(newItem, searchText) ? newItem : null;
  }

  /**
   * Filter a sublist by matching against the searchText.
   * @param sublist The array (sublist) to be filtered.
   * @param searchText The text to search for.
   * @returns The filtered sublist.
   */
  private filterSublist(sublist: any[], searchText: string): any[] {
    // Filter sublist items if they match the searchText
    return sublist.filter(subitem => this.hasMatch(subitem, searchText));
  }

  /**
   * Check if the item or any of its properties match the searchText.
   * @param item The item to check for matches.
   * @param searchText The text to search for.
   * @returns True if a match is found, otherwise false.
   */
  private hasMatch(item: any, searchText: string): boolean {
    if (item == null) return false;  // Handle null values

    // Convert item to string and check case-insensitive match
    if (typeof item === 'string' || typeof item === 'number' || typeof item === 'boolean') {
      // Normalize the string and compare with search text
      return String(item).toLowerCase().includes(searchText);
    }

    // Handle arrays
    if (Array.isArray(item)) {
      return item.some(el => this.hasMatch(el, searchText));  // Check if any element matches
    }

    // Handle objects by checking their values
    if (typeof item === 'object') {
      return Object.values(item).some(value => this.hasMatch(value, searchText));  // Recurse over object values
    }

    return false;  // No match found
  }
}
