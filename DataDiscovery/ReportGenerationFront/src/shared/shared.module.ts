import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  NgbAccordionModule,
  NgbDatepickerModule,
  NgbDropdownModule,
  NgbModalModule,
  NgbNavModule,
  NgbPaginationModule,
  NgbPopoverModule,
  NgbTooltipModule
} from '@ng-bootstrap/ng-bootstrap';

import { CustomPaginationComponent } from './custom-pagination/custom-pagination.component';
import { DateTimeFormatPipe } from '../app/core/pipes/date-time-format.pipe';
import { FilterPipe } from '../app/core/pipes/filter.pipe';
import { OnlyAlphaDirective } from '../app/core/pipes/only-alpha.directive';
import { OnlyDecimalNumberDirective } from '../app/core/pipes/only-decimal-number.directive';
import { OnlyNumberDirective } from '../app/core/pipes/only-number.directive';
import { LoaderComponent } from '../app/loader/loader.component';
import { AngularEditorModule } from '@kolkov/angular-editor';
import { DeepFilterPipe } from '../app/core/pipes/deep-filter.pipe';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { InfiniteScrollDirective } from 'ngx-infinite-scroll';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    NgbPopoverModule,
    NgbModalModule,
    NgbDatepickerModule,
    NgbDropdownModule,
    NgbTooltipModule,
    NgbNavModule,
    AngularEditorModule,
    NgbAccordionModule,
    NgbPaginationModule,
    AngularMultiSelectModule,
    InfiniteScrollDirective,
  ],
  declarations: [
    LoaderComponent,
    CustomPaginationComponent,
    FilterPipe,
    DeepFilterPipe,
    DateTimeFormatPipe,
    OnlyAlphaDirective,
    OnlyNumberDirective,
    OnlyDecimalNumberDirective
  ],
  exports: [
    LoaderComponent,
    CustomPaginationComponent,
    FilterPipe,
    DeepFilterPipe,
    DateTimeFormatPipe,
    OnlyAlphaDirective,
    OnlyNumberDirective,
    OnlyDecimalNumberDirective,
    

    InfiniteScrollDirective,
    NgbNavModule,
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    AngularEditorModule,
    NgbPopoverModule,
    NgbTooltipModule,
    NgbDatepickerModule,
    NgbDropdownModule,
    NgbAccordionModule,
    NgbPaginationModule,
    AngularMultiSelectModule,
  ]
})
export class SharedModule {}
