# encoding: utf-8
import xlrd
import numpy
import math
import spicy
import xlwt

excelName = ''      # excel name for result stored
permutation = 0.0       # order of data scrambling

# read and preprocess data
def readExcel (filename, col_name_index = 0, by_index = 0) :
    data = xlrd.open_workbook(filename)
    table = data.sheets()[by_index]     # the excel data
    ncols = table.ncols     # the rows number of the data
    nrows = table.nrows     # the cols number of the data
    col_Name = table.row_values(col_name_index)     #the first row as an index

    # number of CPU cores
    c = numpy.matrix(table.col_values(col_Name.index('cm')))
    c = c[:, 1:].T
    c = numpy.mat([int(x) for x in c]).T

    # size of used RAM at t time
    r_t = numpy.matrix(table.col_values(col_Name.index('r(t4)')))
    r_t = r_t[:, 1:].T
    r_t = numpy.mat([int(x) for x in r_t]).T

    # size of used buffer at t time
    b_t = numpy.matrix(table.col_values(col_Name.index('b(t4)')))
    b_t = b_t[:, 1:].T
    b_t = numpy.mat([int(x) for x in b_t]).T

    # number of records in the database at t time
    Nr_4 = numpy.matrix(table.col_values(col_Name.index('Nr(t4)')))
    Nr_4 = Nr_4[:, 1:].T
    Nr_4 = numpy.mat([int(x) for x in Nr_4]).T

    # multi-programming level of database
    y = numpy.matrix(table.col_values(col_Name.index('y')))
    y = y[:, 1:].T
    y = numpy.mat([int(x) for x in y]).T

    # time interval
    t_4 = numpy.matrix(table.col_values(col_Name.index('t4')))
    t_4 = t_4[:, 1:].T
    t_4 = numpy.mat([int(x) for x in t_4]).T
    t_5 = numpy.matrix(table.col_values(col_Name.index('t5')))
    t_5 = t_5[:, 1:].T
    t_5 = numpy.mat([int(x) for x in t_5]).T
    t = t_5 - t_4

    # column vectors of constant 1
    constant = numpy.mat(numpy.ones(nrows-1)).T

    # data normalization
    Nr_4 = normalization(Nr_4)
    y = normalization(y)
    c = normalization(c)
    r_t = normalization(r_t)
    b_t = normalization(b_t)
    t = normalization(t)

    # the data set
    xMat = numpy.hstack((constant, Nr_4, y, c, r_t, b_t))
    yMat = t

    # disruption of data sequence
    global permutation
    xMat = xMat[permutation, :]
    yMat = yMat[permutation]

    return xMat, yMat

# regression equation
def regression(xMat, yMat):
    yHat = lwlrTest (xMat, yMat)

# locally weighted linear regression
def lwlr(testPoint, xMat, yMat, k=0.05):     # k controls the decay rate
    m = numpy.shape(xMat)[0]
    weights = numpy.mat(numpy.eye((m)))          # weight matrix

    for j in range(m):
        diffMat = testPoint - xMat[j, :]
        weights[j, j] = math.exp(diffMat * diffMat.T / (-2.0*k**2))

    xTx = xMat.T * (weights * xMat)
    ws = xTx.I * (xMat.T * (weights * yMat))                # ws as wegression coefficient
    return testPoint * ws

def lwlrTest(xMat, yMat, k=0.05):
    [x, y] = numpy.shape(xMat)
    m = int(x * 0.1)
    xArr = xMat[:x - m, :]
    yArr = yMat[:x - m, :]
    TestArr = xMat[x - m:, :]
    yTestArr = yMat[x - m:, :]

    yHat = numpy.zeros(m)
    for i in range(m):
        yHat[i] = lwlr(TestArr[i], xArr, yArr, k)       # predictive value

    yHat = numpy.mat(yHat).T
    data = numpy.hstack((yHat, yTestArr))

    # check fitting effect
    corrcoef = numpy.corrcoef(yHat.T, yTestArr.T)       # correlation coefficient
    arg_sqrt = math.sqrt(spicy.mean(yHat - yTestArr) ** 2)      # root mean square error
    R2 = 1 - (numpy.square(yHat - yTestArr)).sum() / (numpy.square(yHat - yTestArr.mean())).sum()       # R square

    WriteExcel(data, corrcoef[0, 1], arg_sqrt, R2)

# write data to excel file
def WriteExcel(data, corrcoef, arg_sqrt, R2):
    global excelName
    workbook = xlwt.Workbook()
    sheet = workbook.add_sheet('testData1', cell_overwrite_ok=True)
    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            sheet.write(i, j, data[i, j])
    sheet.write(1, 5, corrcoef)
    sheet.write(2, 5, arg_sqrt)
    sheet.write(3, 5, R2)
    workbook.save(excelName)

# normalization
def normalization(data):
    max = numpy.max(data)
    max_log = math.log(float(max))
    data = numpy.mat([math.log(float(x)+0.1)/max_log for x in data]).T

    return data

# get excel name for result stored
def getExcelName(str):
    global excelName
    excelName = str

# get order of data scrambling
def getPermutation(num):
    global permutation
    permutation = num