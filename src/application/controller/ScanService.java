package application.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public abstract class ScanService extends Service<Void> {

  @Override
  public void reset() {
    super.reset();
    initUI();
  }

  protected abstract void initUI();

  @Override
  protected abstract Task<Void> createTask();

  protected abstract void showSuccess(String message);

  protected abstract void showError(String message);

  protected abstract void hideIndicators();

  protected abstract String getSuccessMessage();

  protected abstract String getErrorMessage();

  @Override
  protected void succeeded() {
    hideIndicators();
    showSuccess(getSuccessMessage());
  }

  @Override
  protected void failed() {
    hideIndicators();
    showError(getErrorMessage());
  }

}
