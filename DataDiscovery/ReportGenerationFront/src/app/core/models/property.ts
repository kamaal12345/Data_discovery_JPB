export interface Property {
    id: number;
    name: string;
    lookupCodeSetValue: number;
    createdById: number;
    updatedById: number;
    status: boolean;
    propertyValues: PropertyValue[];
}

export interface PropertyValue {
    id: number;
    description: string;
    lookupCodeSetId: number;
    value: number;
    createdById: number;
    updatedById: null;
    status: boolean;
}
