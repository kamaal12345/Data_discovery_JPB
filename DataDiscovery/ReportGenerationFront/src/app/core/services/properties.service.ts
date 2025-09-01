import { Injectable } from '@angular/core';
import { Observable, catchError, forkJoin, map } from 'rxjs';
import { ApiService } from './api.service';
import { PaginationRequest } from '../models/PaginationRequest';

@Injectable({
  providedIn: 'root'
})
export class PropertiesService {

  url: string = '/properties';

  constructor(
    private apiService: ApiService
  ) { }

  /**
   * get propertys list
   * @returns
   */
  propertyList(body: PaginationRequest): Observable<any> {
    let url = this.url + '/properties-list';
    url += '?offset=' + (body.offset ? body.offset : 0);
    url += '&pageSize=' + (body.pageSize ? body.pageSize : '');
    url += '&field=' + (body.field ? body.field : '');
    url += '&sort=' + (body.sort ? body.sort : 0);
    return this.apiService.get(url)
      .pipe(
        map(res => {
          return <any>res;
        }),
        catchError(err => this.apiService.handleError(err)));
  }

  /**
   * view property
   * @param propertyId
   * @returns
   */
   viewProperty(propertyId: number): Observable<any> {
    return this.apiService.get(this.url + '/' + propertyId);
  }

  /**
   * by value - view property details by value
   * @param propertyValue
   * @returns
   */
   viewPropertyDetailsByValue(propertyValue: number): Observable<any> {
    return this.apiService.get(this.url + '/propertyValues/' + propertyValue);
  }

  /**
   * add property
   * @param body
   * @returns
   */
  addProperty(body: any): Observable<any> {
    return this.apiService.post(this.url + '/add', body);
  }

  /**
   * edit property
   * @param body
   * @returns
   */
   editProperty(body: any): Observable<any> {
    return this.apiService.put(this.url + '/edit', body);
  }

  /**
   * @param ids - property ids list
   * @returns - property values list
   */
  public getPropertyValuesListFromMultipleIds(ids: number[]): Observable<any[]> {
    let respose: any[] = [];
    ids.forEach(element => {
      respose.push(this.viewPropertyDetailsByValue(element));
    });
    return forkJoin(respose);
  }
}
