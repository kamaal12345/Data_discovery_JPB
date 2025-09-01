import { ChangeDetectorRef, Component } from '@angular/core';
import { finalize, Subject, takeUntil } from 'rxjs';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import {
  ModalDismissReasons,
  NgbModal,
  NgbModalRef,
} from '@ng-bootstrap/ng-bootstrap';
import { HostsAddComponent } from '../hosts-add/hosts-add.component';
import { SharedModule } from '../../../../../shared/shared.module';
import { PaginationRequest } from '../../../../core/models/PaginationRequest';
import { PaginationResponseData } from '../../../../core/models/PaginationResponseData';
import { GlobalService } from '../../../../core/services/global-service.service';
import { HostsService } from '../../../../core/services/hosts.service';
import { Host } from '../../../../core/models/Host';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import editorConfig from '../../../../../shared/editor-config';

@Component({
  selector: 'app-hosts-list',
  standalone: true,
  imports: [SharedModule, HostsAddComponent],
  templateUrl: './hosts-list.component.html',
  styleUrl: './hosts-list.component.css',
})
export class HostsListComponent {
  isLoading: boolean;
  closeResult: string;
  public destroy$: Subject<boolean>;
  paginationRequest: PaginationRequest;
  paginationResponseData: PaginationResponseData;
  page: number;
  pageLimits: number[];
  order: string;
  currentSort: string;
  totalResult: number;
  searchText: string;
  showSort = true;
  hostsList: Host[];
  accordionVisibility: boolean[] = [];
  currentIndex: number = 0;
  showError: boolean = false;
  hostDetails: Host;
  idToEdit: number;

  constructor(
    public globalService: GlobalService,
    public hostsService: HostsService,
    private spinner: NgxUiLoaderService,
    private modalService: NgbModal,
    private cdr: ChangeDetectorRef
  ) {
    this.isLoading = false;
    this.closeResult = '';
    this.destroy$ = new Subject<boolean>();
    this.hostsList = [];
    this.paginationRequest = {} as PaginationRequest;
    this.paginationResponseData = {} as PaginationResponseData;
    this.pageLimits = this.globalService.getPageLimits();
  }

  ngOnInit(): void {
    this.getAllHostList();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  searchMatchedData() {
    this.destroy$.next(true);
    this.getAllHostList();
  }

  getAllHostList(): void {
    this.isLoading = true;
    this.spinner.start();
    this.hostsService
      .getHostsList()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
          this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          this.hostsList = data;
          this.cdr.detectChanges();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  OpenAddHosts(content: any, size: any, id?: any) {
    this.idToEdit = id || null;
    this.modalService
      .open(content, {
        ariaLabelledBy: 'modal-basic-title',
        size: size,
        backdrop: 'static',
        keyboard: false,
      })
      .result.then(
        (result) => {
          this.closeResult = `Closed with: ${result}`;
          this.getAllHostList();
        },
        (reason) => {
          this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
        }
      );
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }
}
