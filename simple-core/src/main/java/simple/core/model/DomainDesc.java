package simple.core.model;

import java.util.List;

import simple.core.model.annotation.DataFilterDesc;
import simple.core.model.annotation.OperationDesc;
import simple.core.model.annotation.StandarOperationDesc;

public class DomainDesc {

	private String label;

	private List<UpdateImportDesc> updateImport;

	private StandarOperationDesc standarOperation;

	private List<OperationDesc> operation;

	private transient DataFilterDesc dataFilter;

	private transient boolean batch = true;

	private String defaultSort;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<UpdateImportDesc> getUpdateImport() {
		return updateImport;
	}

	public void setUpdateImport(List<UpdateImportDesc> updateImport) {
		this.updateImport = updateImport;
	}

	public StandarOperationDesc getStandarOperation() {
		return standarOperation;
	}

	public void setStandarOperation(StandarOperationDesc standarOperation) {
		this.standarOperation = standarOperation;
	}

	public List<OperationDesc> getOperation() {
		return operation;
	}

	public void setOperation(List<OperationDesc> operation) {
		this.operation = operation;
	}

	public DataFilterDesc getDataFilter() {
		return dataFilter;
	}

	public void setDataFilter(DataFilterDesc dataFilter) {
		this.dataFilter = dataFilter;
	}

	public String getDefaultSort() {
		return defaultSort;
	}

	public void setDefaultSort(String defaultSort) {
		this.defaultSort = defaultSort;
	}

	public boolean isBatch() {
		return batch;
	}

	public void setBatch(boolean batch) {
		this.batch = batch;
	}

}
