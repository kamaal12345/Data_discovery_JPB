export interface Users {
    userId: number;
    username: string
    password: string | null;
    employeeId: string;
    firstName: string;
    middleName: string;
    lastName: string;
    email: string;
    mobilePhone: string;
    gender : number;
    dob : number;
    status: 0 | 1;
    comments: string;
    profileImg: string;
    designation: number[];
    // department: number;
    roleValue: number[];
    loggedIn: 0 | 1;
    metaStatus: 0 | 1;
    createdById: number;
    updatedById: number;
    createdDate: number;
    updatedDate: number;
    

}