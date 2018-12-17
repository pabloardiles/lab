import { TestBed } from '@angular/core/testing';

import { TrainmeupService } from './trainmeup.service';

describe('TrainmeupService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TrainmeupService = TestBed.get(TrainmeupService);
    expect(service).toBeTruthy();
  });
});
