package org.waterforpeople.mapping.dataexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto.QuestionType;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionOptionDto;
import org.waterforpeople.mapping.app.gwt.client.survey.TranslationDto;
import org.waterforpeople.mapping.app.gwt.client.surveyinstance.SurveyInstanceDto;
import org.waterforpeople.mapping.app.web.dto.SurveyRestRequest;
import org.waterforpeople.mapping.dataexport.service.BulkDataServiceClient;

import com.gallatinsystems.common.util.JFreechartChartUtil;

/**
 * Enhancement of the SurveySummaryExporter to support writing to Excel and
 * including chart images.
 * 
 * @author Christopher Fagiani
 * 
 */
public class GraphicalSurveySummaryExporter extends SurveySummaryExporter {
	private static final String IMAGE_PREFIX_OPT = "imgPrefix";
	private static final String LOCALE_OPT = "locale";

	private static final String DEFAULT_IMAGE_PREFIX = "http://waterforpeople.s3.amazonaws.com/images/";
	private static final String SDCARD_PREFIX = "/sdcard/";

	private static final Map<String, String> REPORT_HEADER;
	private static final Map<String, String> FREQ_LABEL;
	private static final Map<String, String> PCT_LABEL;
	private static final Map<String, String> SUMMARY_LABEL;
	private static final Map<String, String> RAW_DATA_LABEL;
	private static final Map<String, String> INSTANCE_LABEL;
	private static final Map<String, String> SUB_DATE_LABEL;
	private static final Map<String, String> SUBMITTER_LABEL;
	private static final Map<String, String> MEAN_LABEL;
	private static final Map<String, String> MODE_LABEL;
	private static final Map<String, String> MEDIAN_LABEL;
	private static final Map<String, String> MIN_LABEL;
	private static final Map<String, String> MAX_LABEL;
	private static final Map<String, String> VAR_LABEL;
	private static final Map<String, String> STD_E_LABEL;
	private static final Map<String, String> STD_D_LABEL;
	private static final Map<String, String> TOTAL_LABEL;
	private static final Map<String, String> RANGE_LABEL;

	private static final int CHART_WIDTH = 600;
	private static final int CHART_HEIGHT = 400;
	private static final int CHART_CELL_WIDTH = 10;
	private static final int CHART_CELL_HEIGHT = 22;
	private static final String DEFAULT_LOCALE = "en";
	private static final String DEFAULT = "default";

	static {
		// populate all translations
		RANGE_LABEL = new HashMap<String, String>();
		RANGE_LABEL.put("en", "Range");
		RANGE_LABEL.put("es", "Distribución");

		MEAN_LABEL = new HashMap<String, String>();
		MEAN_LABEL.put("en", "Mean");
		MEAN_LABEL.put("es", "Media");

		MODE_LABEL = new HashMap<String, String>();
		MODE_LABEL.put("en", "Mode");
		MODE_LABEL.put("es", "Moda");

		MEDIAN_LABEL = new HashMap<String, String>();
		MEDIAN_LABEL.put("en", "Median");
		MEDIAN_LABEL.put("es", "Número medio");

		MIN_LABEL = new HashMap<String, String>();
		MIN_LABEL.put("en", "Min");
		MIN_LABEL.put("es", "Mínimo");

		MAX_LABEL = new HashMap<String, String>();
		MAX_LABEL.put("en", "Max");
		MAX_LABEL.put("es", "Máximo");

		VAR_LABEL = new HashMap<String, String>();
		VAR_LABEL.put("en", "Variance");
		VAR_LABEL.put("es", "Varianza");

		STD_D_LABEL = new HashMap<String, String>();
		STD_D_LABEL.put("en", "Std Deviation");
		STD_D_LABEL.put("es", "Desviación Estándar");

		STD_E_LABEL = new HashMap<String, String>();
		STD_E_LABEL.put("en", "Std Error");
		STD_E_LABEL.put("es", "Error Estándar");

		TOTAL_LABEL = new HashMap<String, String>();
		TOTAL_LABEL.put("en", "Total");
		TOTAL_LABEL.put("es", "Suma");

		REPORT_HEADER = new HashMap<String, String>();
		REPORT_HEADER.put("en", "Survey Summary Report");
		REPORT_HEADER.put("es", "Encuesta Informe Resumen");

		FREQ_LABEL = new HashMap<String, String>();
		FREQ_LABEL.put("en", "Frequency");
		FREQ_LABEL.put("es", "Frecuencia");

		PCT_LABEL = new HashMap<String, String>();
		PCT_LABEL.put("en", "Percent");
		PCT_LABEL.put("es", "Por ciento");

		SUMMARY_LABEL = new HashMap<String, String>();
		SUMMARY_LABEL.put("en", "Summary");
		SUMMARY_LABEL.put("es", "Resumen");

		RAW_DATA_LABEL = new HashMap<String, String>();
		RAW_DATA_LABEL.put("en", "Raw Data");
		RAW_DATA_LABEL.put("es", "Primas de Datos");

		INSTANCE_LABEL = new HashMap<String, String>();
		INSTANCE_LABEL.put("en", "Instance");
		INSTANCE_LABEL.put("es", "Instancia");

		SUB_DATE_LABEL = new HashMap<String, String>();
		SUB_DATE_LABEL.put("en", "Submission Date");
		SUB_DATE_LABEL.put("es", "Fecha de presentación");

		SUBMITTER_LABEL = new HashMap<String, String>();
		SUB_DATE_LABEL.put("en", "Submitter");
		SUB_DATE_LABEL.put("es", "Peticionario");

	}

	private static final NumberFormat PCT_FMT = DecimalFormat
			.getPercentInstance();

	private HSSFCellStyle headerStyle;
	private String locale;
	private String imagePrefix;
	private String serverBase;

	@Override
	public void export(Map<String, String> criteria, File fileName,
			String serverBase, Map<String, String> options) {
		processOptions(options);
		this.serverBase = serverBase;
		PrintWriter pw = null;
		try {
			Map<QuestionGroupDto, List<QuestionDto>> questionMap = loadAllQuestions(
					criteria.get(SurveyRestRequest.SURVEY_ID_PARAM), serverBase);
			if (!DEFAULT_LOCALE.equals(locale) && questionMap.size() > 0) {
				// if we are using some other locale, we need to check for
				// translations
				loadFullQuestions(questionMap);
			}
			if (questionMap.size() > 0) {
				HSSFWorkbook wb = new HSSFWorkbook();
				headerStyle = wb.createCellStyle();
				headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				HSSFFont headerFont = wb.createFont();
				headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				headerStyle.setFont(headerFont);

				SummaryModel model = fetchAndWriteRawData(
						criteria.get(SurveyRestRequest.SURVEY_ID_PARAM),
						serverBase, questionMap, wb);
				writeSummaryReport(questionMap, model, null, wb);
				if (model.getSectorList() != null
						&& model.getSectorList().size() > 0) {
					for (String sector : model.getSectorList()) {
						writeSummaryReport(questionMap, model, sector, wb);
					}
				}
				FileOutputStream fileOut = new FileOutputStream(fileName);
				wb.setActiveSheet(1);
				wb.write(fileOut);
				fileOut.close();
			} else {
				System.out.println("No questions for survey");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected SummaryModel fetchAndWriteRawData(String surveyId,
			String serverBase,
			Map<QuestionGroupDto, List<QuestionDto>> questionMap,
			HSSFWorkbook wb) throws Exception {
		SummaryModel model = new SummaryModel();

		HSSFSheet sheet = wb.createSheet(RAW_DATA_LABEL.get(locale));
		Object[] results = createRawDataHeader(wb, sheet, questionMap);
		List<String> questionIdList = (List<String>) results[0];
		List<String> unsummarizable = (List<String>) results[1];
		int curRow = 1;

		Map<String, String> instanceMap = BulkDataServiceClient
				.fetchInstanceIds(surveyId, serverBase);

		for (Entry<String, String> instanceEntry : instanceMap.entrySet()) {
			String instanceId = instanceEntry.getKey();
			String dateString = instanceEntry.getValue();
			HSSFRow row = getRow(curRow++, sheet);
			Map<String, String> responseMap = BulkDataServiceClient
					.fetchQuestionResponses(instanceId, serverBase);
			int col = 0;

			if (responseMap != null && responseMap.size() > 0) {
				createCell(row, col++, instanceId, null);
				createCell(row, col++, dateString, null);
				SurveyInstanceDto dto = BulkDataServiceClient
						.findSurveyInstance(Long.parseLong(instanceId.trim()),
								serverBase);
				if (dto != null) {
					String name = dto.getSubmitterName();
					if (name != null) {
						createCell(row, col++, dto.getSubmitterName()
								.replaceAll("\n", " ").trim(), null);
					} else {
						createCell(row, col++, " ", null);
					}
				}
				for (String q : questionIdList) {
					String val = responseMap.get(q);
					if (val != null) {
						if (val.contains(SDCARD_PREFIX)) {
							val = imagePrefix
									+ val.substring(val.indexOf(SDCARD_PREFIX)
											+ SDCARD_PREFIX.length());
						}
						createCell(row, col++,
								val.replaceAll("\n", " ").trim(), null);
					} else {
						createCell(row, col++, "", null);
					}
				}

				String sector = "";
				if (sectorQuestion != null) {
					sector = responseMap.get(sectorQuestion.getKeyId()
							.toString());
				}
				for (Entry<String, String> entry : responseMap.entrySet()) {
					if (!unsummarizable.contains(entry.getKey())) {
						model.tallyResponse(entry.getKey(), sector,
								entry.getValue());
					}
				}
			}
		}
		return model;
	}

	/**
	 * creates the header for the raw data tab
	 * 
	 * @param row
	 * @param questionMap
	 * @return - returns a 2 element array. The first element is a List of
	 *         String objects representing all the question Ids. The second
	 *         element is a List of Strings representing all the non-sumarizable
	 *         question Ids (i.e. those that aren't OPTION or NUMBER questions)
	 */
	private Object[] createRawDataHeader(HSSFWorkbook wb, HSSFSheet sheet,
			Map<QuestionGroupDto, List<QuestionDto>> questionMap) {
		HSSFRow row = getRow(0, sheet);
		createCell(row, 0, INSTANCE_LABEL.get(locale), headerStyle);
		createCell(row, 1, SUB_DATE_LABEL.get(locale), headerStyle);
		createCell(row, 2, SUBMITTER_LABEL.get(locale), headerStyle);
		List<String> questionIdList = new ArrayList<String>();
		List<String> nonSummarizableList = new ArrayList<String>();
		if (questionMap != null) {
			int offset = 3;
			for (Entry<QuestionGroupDto, List<QuestionDto>> entry : questionMap
					.entrySet()) {
				if (entry.getValue() != null) {
					for (QuestionDto q : entry.getValue()) {
						questionIdList.add(q.getKeyId().toString());
						createCell(
								row,
								offset++,
								q.getKeyId().toString()
										+ "|"
										+ getLocalizedText(q.getText(),
												q.getTranslationMap())
												.replaceAll("\n", "").trim(),
								headerStyle);
						if (!(QuestionType.NUMBER == q.getType() || QuestionType.OPTION == q
								.getType())) {
							nonSummarizableList.add(q.getKeyId().toString());
						}
					}
				}
			}
		}
		Object[] temp = new Object[2];
		temp[0] = questionIdList;
		temp[1] = nonSummarizableList;
		return temp;
	}

	/**
	 * 
	 * Writes the report as an XLS document
	 */
	private void writeSummaryReport(
			Map<QuestionGroupDto, List<QuestionDto>> questionMap,
			SummaryModel summaryModel, String sector, HSSFWorkbook wb)
			throws Exception {

		HSSFSheet sheet = wb.createSheet(sector == null ? SUMMARY_LABEL
				.get(locale) : sector);
		HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
		int curRow = 0;
		HSSFRow row = getRow(curRow++, sheet);
		if (sector == null) {
			createCell(row, 0, REPORT_HEADER.get(locale), headerStyle);
		} else {
			createCell(row, 0, sector + " " + REPORT_HEADER.get(locale),
					headerStyle);
		}
		for (Entry<QuestionGroupDto, List<QuestionDto>> mapEntry : questionMap
				.entrySet()) {
			if (mapEntry.getValue() != null) {
				for (QuestionDto question : mapEntry.getValue()) {
					if (!(QuestionType.OPTION == question.getType() || QuestionType.NUMBER == question
							.getType())) {
						continue;
					}
					// for both options and numeric, we want a pie chart and
					// data table for numeric, we also want descriptive
					// statistics
					int tableTopRow = curRow++;
					int tableBottomRow = curRow;
					row = getRow(tableTopRow, sheet);
					// span the question heading over the data table
					sheet.addMergedRegion(new CellRangeAddress(curRow - 1,
							curRow - 1, 0, 2));
					createCell(
							row,
							0,
							getLocalizedText(question.getText(),
									question.getTranslationMap()), headerStyle);
					DescriptiveStats stats = summaryModel
							.getDescriptiveStatsForQuestion(
									question.getKeyId(), sector);
					if (stats != null && stats.getSampleCount() > 0) {
						sheet.addMergedRegion(new CellRangeAddress(curRow - 1,
								curRow - 1, 4, 5));
						createCell(
								row,
								4,
								getLocalizedText(question.getText(),
										question.getTranslationMap()),
								headerStyle);
					}
					row = getRow(curRow++, sheet);
					createCell(row, 1, FREQ_LABEL.get(locale), headerStyle);
					createCell(row, 2, PCT_LABEL.get(locale), headerStyle);

					// now create the data table for the option count
					Map<String, Long> counts = summaryModel
							.getResponseCountsForQuestion(question.getKeyId(),
									sector);
					int sampleTotal = 0;
					List<String> labels = new ArrayList<String>();
					List<String> values = new ArrayList<String>();
					int firstOptRow = curRow;
					for (Entry<String, Long> count : counts.entrySet()) {
						row = getRow(curRow++, sheet);
						String labelText = count.getKey();
						if (QuestionType.OPTION == question.getType() && !DEFAULT_LOCALE.equals(locale)){							
							//see if we have a translation for this option
							if(question.getOptionContainerDto()!=null && question.getOptionContainerDto().getOptionsList()!=null){
								for(QuestionOptionDto opt: question.getOptionContainerDto().getOptionsList()){
									if(opt.getText()!=null && opt.getText().trim().equalsIgnoreCase(labelText)){
										labelText = getLocalizedText(labelText, opt.getTranslationMap());
										break;
									}
								}							
							}
						}
						createCell(row, 0, labelText, null);						
						createCell(row, 1, count.getValue().toString(), null);
						
						labels.add(labelText);
						values.add(count.getValue().toString());
						sampleTotal += count.getValue();
					}
					row = getRow(curRow++, sheet);
					createCell(row, 0, TOTAL_LABEL.get(locale), null);
					createCell(row, 1, sampleTotal + "", null);
					for (int i = 0; i < values.size(); i++) {
						row = getRow(firstOptRow + i, sheet);
						if (sampleTotal > 0) {
							createCell(row, 2,
									PCT_FMT.format((Double.parseDouble(values
											.get(i)) / (double) sampleTotal)),
									null);
						} else {
							createCell(row, 2, PCT_FMT.format(0), null);
						}
					}

					tableBottomRow = curRow;

					if (stats != null && stats.getSampleCount() > 0) {
						int tempRow = tableTopRow + 1;
						row = getRow(tempRow++, sheet);
						createCell(row, 4, "N", null);
						createCell(row, 5, sampleTotal + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, MEAN_LABEL.get(locale), null);
						createCell(row, 5, stats.getMean() + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, STD_E_LABEL.get(locale), null);
						createCell(row, 5, stats.getStandardError() + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, MEDIAN_LABEL.get(locale), null);
						createCell(row, 5, stats.getMedian() + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, MODE_LABEL.get(locale), null);
						createCell(row, 5, stats.getMode() + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, STD_D_LABEL.get(locale), null);
						createCell(row, 5, stats.getStandardDeviation() + "",
								null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, VAR_LABEL.get(locale), null);
						createCell(row, 5, stats.getVariance() + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, RANGE_LABEL.get(locale), null);
						createCell(row, 5, stats.getRange() + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, MIN_LABEL.get(locale), null);
						createCell(row, 5, stats.getMin() + "", null);
						row = getRow(tempRow++, sheet);
						createCell(row, 4, MAX_LABEL.get(locale), null);
						createCell(row, 5, stats.getMax() + "", null);
						if (tableBottomRow < tempRow) {
							tableBottomRow = tempRow;
						}
					}
					curRow = tableBottomRow;
					if (labels.size() > 0) {
						// now insert the graph
						int indx = wb.addPicture(JFreechartChartUtil
								.getPieChart(
										labels,
										values,
										getLocalizedText(question.getText(),
												question.getTranslationMap()),
										CHART_WIDTH, CHART_HEIGHT),
								HSSFWorkbook.PICTURE_TYPE_PNG);
						HSSFClientAnchor anchor;
						anchor = new HSSFClientAnchor(0, 0, 0, 255, (short) 6,
								tableTopRow, (short) (6 + CHART_CELL_WIDTH),
								tableTopRow + CHART_CELL_HEIGHT);
						anchor.setAnchorType(2);
						patriarch.createPicture(anchor, indx);
						if (tableTopRow + CHART_CELL_HEIGHT > tableBottomRow) {
							curRow = tableTopRow + CHART_CELL_HEIGHT;
						}
					}

					// add a blank row between questions
					getRow(curRow++, sheet);

				}
			}
		}
	}

	/**
	 * creates a cell in the row passed in and sets the style and value (if
	 * non-null)
	 * 
	 */
	private HSSFCell createCell(HSSFRow row, int col, String value,
			HSSFCellStyle style) {
		HSSFCell cell = row.createCell(col);
		if (style != null) {
			cell.setCellStyle(style);
		}
		if (value != null) {
			cell.setCellValue(value);
		}
		
		return cell;
	}

	/**
	 * finds or creates the row at the given index
	 * 
	 * @param index
	 * @param rowLocalMax
	 * @param sheet
	 * @return
	 */
	private HSSFRow getRow(int index, HSSFSheet sheet) {
		HSSFRow row = null;
		if (index < sheet.getLastRowNum()) {
			row = sheet.getRow(index);
		} else {
			row = sheet.createRow(index);
		}
		return row;

	}

	/**
	 * sets instance variables to the values passed in in the Option map. If the
	 * option is not set, the default values are used.
	 * 
	 * @param options
	 */
	private void processOptions(Map<String, String> options) {
		if (options != null) {
			locale = options.get(LOCALE_OPT);
			imagePrefix = options.get(IMAGE_PREFIX_OPT);
		}
		if (locale != null) {
			locale = locale.trim().toLowerCase();
			if (DEFAULT.equalsIgnoreCase(locale)) {
				locale = DEFAULT_LOCALE;
			}

		} else {
			locale = DEFAULT_LOCALE;
		}
		if (imagePrefix != null) {
			imagePrefix = imagePrefix.trim();
		} else {
			imagePrefix = DEFAULT_IMAGE_PREFIX;
		}
	}

	/**
	 * call the server to augment the data already loaded in each QuestionDto in
	 * the map passed in.
	 * 
	 * @param questionMap
	 */
	private void loadFullQuestions(
			Map<QuestionGroupDto, List<QuestionDto>> questionMap) {
		for (List<QuestionDto> questionList : questionMap.values()) {
			for (int i = 0; i < questionList.size(); i++) {
				try {
					QuestionDto newQ = BulkDataServiceClient
							.loadQuestionDetails(serverBase, questionList
									.get(i).getKeyId());
					if (newQ != null) {
						questionList.set(i, newQ);
					}
				} catch (Exception e) {
					System.err.println("Could not fetch question details");
					e.printStackTrace(System.err);
				}
			}
		}
	}

	/**
	 * uses the locale and the translation map passed in to determine what value
	 * to use for the string
	 * 
	 * @param text
	 * @param translationMap
	 * @return
	 */
	private String getLocalizedText(String text,
			Map<String, TranslationDto> translationMap) {
		TranslationDto trans = null;
		if (translationMap != null) {
			trans = translationMap.get(locale);
		}
		if (trans != null && trans.getText() != null
				&& trans.getText().trim().length() > 0) {
			return trans.getText();
		} else {
			return text;

		}
	}
}