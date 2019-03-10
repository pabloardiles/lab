import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AnswerMainComponent } from './answer-main.component';

describe('AnswerMainComponent', () => {
  let component: AnswerMainComponent;
  let fixture: ComponentFixture<AnswerMainComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AnswerMainComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AnswerMainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
