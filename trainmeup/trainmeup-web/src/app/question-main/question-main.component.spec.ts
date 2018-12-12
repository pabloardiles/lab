import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionMainComponent } from './question-main.component';

describe('QuestionMainComponent', () => {
  let component: QuestionMainComponent;
  let fixture: ComponentFixture<QuestionMainComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionMainComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionMainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
