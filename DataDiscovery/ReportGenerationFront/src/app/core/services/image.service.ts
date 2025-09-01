import { Injectable } from '@angular/core';
import { DocumentImage } from '../models/DocumentImage';
import { environment } from '../../../environments/environment';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ImageService {
  url: string = '/image';

  constructor(private apiService: ApiService) {}

  /**
   * get only image  list
   * @returns
   */
  imageList(): Observable<any> {
    return this.apiService.get(this.url + '/all');
  }

  /**
   * view only id
   * @param id
   * @returns
   */
  getImageData(id: number): Observable<any> {
    return this.apiService.get(this.url + '/' + id);
  }

  /**
   * add only image
   * @param body
   * @returns
   */
  addDocumentImage(body: DocumentImage): Observable<any> {
    return this.apiService.post(this.url + '/add-document', body);
  }

  /**
   * change image
   * @param id
   * @returns
   */
  changeDocumentImage(body: DocumentImage): Observable<any> {
    return this.apiService.put(
      this.url + '/edit-documentImage/' + body.id,
      body
    );
  }
}
