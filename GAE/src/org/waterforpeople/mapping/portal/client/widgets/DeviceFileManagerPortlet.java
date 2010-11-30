package org.waterforpeople.mapping.portal.client.widgets;

import java.util.ArrayList;

import org.waterforpeople.mapping.app.gwt.client.devicefiles.DeviceFilesDto;
import org.waterforpeople.mapping.app.gwt.client.devicefiles.DeviceFilesService;
import org.waterforpeople.mapping.app.gwt.client.devicefiles.DeviceFilesServiceAsync;
import org.waterforpeople.mapping.app.gwt.client.user.UserDto;

import com.gallatinsystems.framework.gwt.component.DataTableBinder;
import com.gallatinsystems.framework.gwt.component.DataTableHeader;
import com.gallatinsystems.framework.gwt.component.DataTableListener;
import com.gallatinsystems.framework.gwt.component.PaginatedDataTable;
import com.gallatinsystems.framework.gwt.dto.client.ResponseDto;
import com.gallatinsystems.framework.gwt.util.client.MessageDialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DeviceFileManagerPortlet extends LocationDrivenPortlet implements
		DataTableBinder<DeviceFilesDto>, DataTableListener<DeviceFilesDto> {
	public static final String DESCRIPTION = "View and Reprocess Device Files";
	public static final String NAME = "Device File Manager Portlet";
	private static final String DEFAULT_SORT_FIELD = "createdDateTime";
	private PaginatedDataTable<DeviceFilesDto> dfTable;

	private static final DataTableHeader HEADERS[] = {
			new DataTableHeader("Id", "key", true),
			new DataTableHeader("URI", "uri", true),
			new DataTableHeader("Checksum", "CheckSum", true),
			new DataTableHeader("Created Date Time", "createDateTime", true),
			new DataTableHeader("Status", "processedStatus", true),
			new DataTableHeader("Processing Message", "processingMessage", true),
			new DataTableHeader("Reprocess") };

	private static final String ANY_OPT = "Any";
	private static final int WIDTH = 1600;
	private static final int HEIGHT = 800;
	private VerticalPanel contentPane;

	public DeviceFileManagerPortlet(String title, boolean scrollable,
			boolean configurable, boolean snapable, int width, int height,
			UserDto user, boolean useCommunity, String specialOption) {
		super(title, scrollable, configurable, snapable, width, height, user,
				useCommunity, specialOption);
		// TODO Auto-generated constructor stub
	}

	VerticalPanel mainVPanel = new VerticalPanel();
	DeviceFilesServiceAsync svc;

	public DeviceFileManagerPortlet(UserDto user) {
		super(NAME, true, false, false, WIDTH, HEIGHT, user, true,
				LocationDrivenPortlet.ANY_OPT);
		contentPane = new VerticalPanel();
		Widget header = buildHeader();
		dfTable = new PaginatedDataTable<DeviceFilesDto>(DEFAULT_SORT_FIELD,
				this, this, true);
		contentPane.add(header);
		setContent(contentPane);
		svc = GWT.create(DeviceFilesService.class);
		dfTable.setVisible(false);
		requestData(null, false);
		mainVPanel.add(dfTable);
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	private Widget buildHeader() {
		Grid grid = new Grid(2, 2);

		grid.setWidget(0, 0, mainVPanel);
		return grid;
	}

	@Override
	public void onItemSelected(DeviceFilesDto item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestData(String cursor, final boolean isResort) {
		final boolean isNew = (cursor == null);
		boolean isOkay = true;
		AsyncCallback<ResponseDto<ArrayList<DeviceFilesDto>>> dataCallback = new AsyncCallback<ResponseDto<ArrayList<DeviceFilesDto>>>() {
			

			@Override
			public void onFailure(Throwable caught) {
				MessageDialog errDia = new MessageDialog("Application Error",
						"Cannot search");
				errDia.showRelativeTo(dfTable);

			}

			@Override
			public void onSuccess(ResponseDto<ArrayList<DeviceFilesDto>> result) {
				dfTable.bindData(result.getPayload(), result.getCursorString(),
						isNew, isResort);

				if (result.getPayload() != null
						&& result.getPayload().size() > 0) {
					dfTable.setVisible(true);
					Button reprocessButton = new Button("Reprocess");
						dfTable.appendRow(reprocessButton);
						reprocessButton.addClickHandler(new ClickHandler() {

							@Override
							public void onClick(ClickEvent event) {
								// TODO Auto-generated method stub
								
							}
							
						});
						
					
				}
			}

			
		};
		svc.listDeviceFiles("PROCESSED_WITH_ERRORS", cursor, dataCallback);
	}

	@Override
	public DataTableHeader[] getHeaders() {
		// TODO Auto-generated method stub
		return HEADERS;
	}

	@Override
	public void bindRow(Grid grid, DeviceFilesDto item, int row) {
		grid.setWidget(row, 0, new Label(item.getKeyId().toString()));
		grid.setWidget(row,1, new Label(item.getPhoneNumber()));
		grid.setWidget(row, 2, new Label(item.getURI()));
		grid.setWidget(row, 3, new Label(item.getProcessedStatus()));
	};

}