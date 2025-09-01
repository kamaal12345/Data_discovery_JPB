import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { finalize, Subject, takeUntil } from 'rxjs';
import { DocumentImage } from '../../core/models/DocumentImage';
import { GlobalService } from '../../core/services/global-service.service';
import { ImageService } from '../../core/services/image.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedModule } from '../../../shared/shared.module';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-image-upload',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './image-upload.component.html',
  styleUrl: './image-upload.component.css',
})
export class ImageUploadComponent implements OnInit, OnDestroy {
  closeResult: string;
  isLoading: boolean;
  public destroy$: Subject<boolean>;
  showError: boolean = false;
  imageObj: DocumentImage;
  imageForm: FormGroup;
  imagesList :DocumentImage[] =[];
  isEdit: boolean;
  idToEdit: number;
  fileExtentionType: string;
  private sanitizer: DomSanitizer;
  isImageView:boolean;
  fileBase64: string;
  errorMsg: string;
  ImageSrc: any;
  constructor(
    public globalService: GlobalService,
    public imageService: ImageService,
    private fb: FormBuilder,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {
    this.closeResult = '';
    this.isLoading = false;
    this.errorMsg = '';
    this.destroy$ = new Subject<boolean>();
    this.isEdit = false;
    this.idToEdit = 0;
    this.imageForm = new FormGroup({});
    this.imageObj = {} as DocumentImage;
    if (this.route.snapshot.params && this.route.snapshot.params['id']) {
      this.isEdit = true;
      this.isImageView = true;
      this.idToEdit = +this.route.snapshot.params['id'];
      console.log(this.idToEdit);
      this.getImageData();
    }
  }

  ngOnInit(): void {
    this.buildForm();
    if (!this.isImageView) {
      this.getALLImagesList();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  buildForm(): void {
    this.imageForm = this.fb.group({
      name: [
        this.imageObj.name,
        [
          Validators.required,
          Validators.minLength(2),
          Validators.pattern('^[a-zA-Z ]*$')
        ],
      ],
    });
  }

  cancel() {
    this.router.navigate(['jrm/report-details/list']);
  }

  getErrorMessage(fieldName: string, fieldLabel: string): string {
    let msg = '';
    msg = this.globalService.getErrorMessage(this.imageForm, fieldName, fieldLabel);
    return msg;
  }
  handleFileSelect(evt: any) {
    if (!this.validateFile(evt)) {
      return;
    }
    const files: any = evt.target.files;
    const file: File = files[0];
    const pattern = /image-*/;
    if (!file.type.match(pattern)) {
      this.globalService.showAlert(
        'Error',
        'Supported formats .jpg or .jpeg or .png !'
      );
      return;
    }
    const FileSize = evt.target.files[0].size / 1024 / 1024; // in MB
    if (FileSize > 5) {
      this.globalService.showAlert('Error', 'File size exceeds 5 MB');
      return;
    }
    if (files && file) {
      const reader = new FileReader();
      // this.imageName = evt.target.files[0].name;
      reader.onload = this._handleReaderLoadedSign.bind(this);
      reader.readAsBinaryString(file);
    }
  }
  _handleReaderLoadedSign(readerEvt: any) {
    const binaryString = readerEvt.target.result;
    this.fileBase64 = btoa(binaryString);
  }
  validateFile(evt: any) {
    const allowedExtension = ['jpeg', 'jpg', 'png'];
    const fileExtension = evt.target.value.split('.').pop().toLowerCase();
    let isValidFile = false;

    for (let index in allowedExtension) {
      if (fileExtension === allowedExtension[index]) {
        this.fileExtentionType = fileExtension;
        isValidFile = true;
        break;
      }
    }

    if (!isValidFile) {
      alert('Allowed Extensions are : *.' + allowedExtension.join(', *.'));
    }
    const FileSize = evt.target.files[0].size / 1024 / 1024; // in MB
    if (FileSize > 5) {
      this.globalService.showAlert('Error', 'File size exceeds 5 MB');
      return null;
    }
    return isValidFile;
  }

  boxVisibility: boolean = true;

  toggleBox() {
    this.boxVisibility = !this.boxVisibility;
  }


  submit(): void {
    this.showError = false;
    this.errorMsg = '';
    if (!this.imageForm.valid) {
      this.globalService.showAlert('Error', 'Please enter all mandatory fields!');
      return;
    }
    this.imageObj.name = this.imageForm.controls['name'].value;
    if (this.fileExtentionType && this.fileBase64) {
      this.imageObj.dataUrl = 'data:application/' + this.fileExtentionType + ';base64,' + this.fileBase64;
    }

    this.isLoading = true;
    if (this.isEdit) {
      this.imageObj.id = this.idToEdit;
      this.imageObj.updatedDate = new Date().getTime();
      this.imageService.changeDocumentImage(this.imageObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => { this.isLoading = false })
        ).subscribe({
          next: (data) => {
            console.log(data);
            if (data) {
              this.globalService.showAlert('Success', 'image updated successfully!');
              this.router.navigate(['report-details/list']);
            } else {
              this.showError = true;
              this.errorMsg = 'Unable to update image!';
              this.globalService.showAlert('Error', 'Unable to update image!');
            }
          },
          error: (e) => {
            console.error(e);
            this.showError = true;
            let msg = e?.error?.message ? e?.error?.message : 'Error in updating image!';
            msg = this.globalService.beautifyErrorMsg(msg);
            this.errorMsg = msg;
            this.globalService.showAlert('Error', msg);
          },
          complete: () => console.info('complete')
        });
    } else {
      this.imageObj.createdDate = new Date().getTime();
      this.imageService.addDocumentImage(this.imageObj)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => { this.isLoading = false })
        ).subscribe({
          next: (data) => {
            console.log(data);
            this.globalService.showAlert('success', 'Successfully Submitted!');
            this.router.navigate(['report-details/list']);
          },
          error: (e) => {
            console.error(e);
            let msg: string;
            if (e.status == 409) {
              msg = e.error?.message || 'Duplicate image name or data URL.';
              this.globalService.showAlert('error', msg);
            } 
            else if (e.status == 500) {
              this.globalService.showAlert('500', 'Oops! There was an error while processing your request');
            }
          },
          complete: () => console.info('complete')
        });
    }
  }

  getImageData() {
    this.isLoading = true;
    this.imageService.getImageData(this.idToEdit)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => { this.isLoading = false })
      ).subscribe({
        next: (data) => {
          console.log(data);
          this.imageObj = data;
          this.buildForm();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete')
      });
  }

  getALLImagesList() {
    this.isLoading = true;
    this.imageService.imageList()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => { this.isLoading = false })
      ).subscribe({
        next: (data) => {
          console.log(data);
          this.imagesList = data;
          if (this.imagesList.length > 0) {
            const firstVideoUrl = this.imagesList[0].dataUrl;
            this.imageClick(firstVideoUrl);
          }
          this.cdr.detectChanges();
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete')
      });
  }

  imageClick(url: string){
    this.ImageSrc = url;
    this.cdr.detectChanges();
  }

}

