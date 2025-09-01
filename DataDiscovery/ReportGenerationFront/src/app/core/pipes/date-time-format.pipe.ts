import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

@Pipe({
  name: 'dateTimeFormat'
})
export class DateTimeFormatPipe implements PipeTransform {

  // adding a default format in case you don't want to pass the format
  // then 'yyyy-MM-dd' will be used
  transform(date: any, isTime?: string): string {
    let format: string, returnStr: any;
    format = 'dd-MM-yyyy';
    if (isTime && isTime.toLowerCase() === 'time') {
      format = 'dd-MM-yyyy hh:mm a';
    } else if (isTime === 'onlyTime') {
      format = 'hh:mm a';
    } else if (isTime === 'onlyMonth') {
      format = 'MMM-yyyy';
    } else if (isTime === 'monthText') {
      format = 'dd-MMM-yyyy';
    } else if (isTime === 'monthTextWithTime') {
      format = 'dd-MMM-yyyy hh:mm a';
    } else if (isTime === 'fullMonthText') {
      format = 'MMMM d, y';
    }

    if (date) {
      returnStr = new DatePipe('en-US').transform(new Date(date), format);
    } else {
      returnStr = null;
    }
    return returnStr;
  }

}
