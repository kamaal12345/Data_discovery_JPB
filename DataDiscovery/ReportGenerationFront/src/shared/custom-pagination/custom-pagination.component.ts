import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-custom-pagination',
  templateUrl: './custom-pagination.component.html',
  styleUrls: ['./custom-pagination.component.css']
})
export class CustomPaginationComponent implements OnInit {

  @Input() collectionSize: number;
  @Input() page: number;
  @Input() pageSize: number;
  @Input() maxSize: number;
  @Output() pageChange: EventEmitter<any> = new EventEmitter();

  constructor() {}

  ngOnInit(): void {
    this.maxSize = this.maxSize || 5;
  }

  onPageChange(event: any) {
    if (Number(event)) {
      this.pageChange.emit(event);
    }
  }

}
