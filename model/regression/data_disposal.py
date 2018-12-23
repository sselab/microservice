import xlrd
import numpy
import xlwt

# read and preprocess data
def readExcel (filename, col_name_index = 0, by_index = 0):
    data = xlrd.open_workbook(filename)
    table = data.sheets()[by_index]     # the excel data

    # real value
    t_real = numpy.matrix(table.col_values(1)).T
    # predictive value
    t_prediction = numpy.matrix(table.col_values(0)).T
    return t_real, t_prediction

if __name__ == '__main__':

    # combine all the predicted values
    for k in range(3):
        excelName = 'Xdata'+str(k+1)+'.xls'     # excel file's name
        workbook = xlwt.Workbook()
        sheet = workbook.add_sheet('inputData', cell_overwrite_ok=True)
        for i in range(2):
            for j in range(10):
                filename = 'test'+str(k+1)+'_Data'+str(j+1)+'_'+str(i+1)+'.xls'
                data = xlrd.open_workbook(filename)
                table = data.sheets()[0]
                t_prediction = numpy.matrix(table.col_values(0)).T
                t_real = numpy.matrix(table.col_values(1)).T
                for r in range(t_prediction.shape[0]):
                    if (t_prediction[r, 0] < 0):
                        sheet.write(t_prediction.shape[0] * 10 * i + t_prediction.shape[0] * j + r, 0, 0)
                    else:
                        sheet.write(t_prediction.shape[0] * 10 * i + t_prediction.shape[0] * j + r, 0,
                                    t_prediction[r, 0])
                    if (t_real[r, 0] < 0):
                        sheet.write(t_real.shape[0] * 10 * i + t_real.shape[0] * j + r, 1, 0)
                    else:
                        sheet.write(t_real.shape[0] * 10 * i + t_real.shape[0] * j + r, 1, t_real[r, 0])
        workbook.save(excelName)

    excelName = 'TotalXdata.xls'     # excel file's name
    workbook = xlwt.Workbook()
    sheet = workbook.add_sheet('Data', cell_overwrite_ok=True)
    for i in range(2):
        for j in range(10):
            filename = 'testTotal' + str(j+1) + '_' + str(i+1) + '.xls'
            data = xlrd.open_workbook(filename)
            table = data.sheets()[0]
            t_prediction = numpy.matrix(table.col_values(0)).T
            t_real = numpy.matrix(table.col_values(1)).T
            for r in range(t_prediction.shape[0]):
                if(t_prediction[r, 0] < 0):
                    sheet.write(t_prediction.shape[0] * 10 * i + t_prediction.shape[0] * j + r, 0, 0)
                else:
                    sheet.write(t_prediction.shape[0] * 10 * i + t_prediction.shape[0] * j + r, 0, t_prediction[r, 0])
                if(t_real[r, 0] < 0):
                    sheet.write(t_real.shape[0] * 10 * i + t_real.shape[0] * j + r, 1, 0)
                else:
                    sheet.write(t_real.shape[0] * 10 * i + t_real.shape[0] * j + r, 1, t_real[r, 0])
    workbook.save(excelName)