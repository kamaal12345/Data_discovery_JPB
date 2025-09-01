import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Roles } from '../models/Roles';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RolesService {

  url: string = '/roles';

  constructor(
    private apiService: ApiService
  ) { }

  /**
   * get roles list
   * @returns
   */
  rolesList(): Observable<Roles[]> {
    return this.apiService.get(this.url + '/roles-list');
  }
}
