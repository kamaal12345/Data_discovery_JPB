export interface PaginationRequest {   
    offset: number;
    pageSize: number;
    field: string;
    sort:number;
    searchText: string;
    startDate: any;
    endDate: any;
    status: number |string ;

}

