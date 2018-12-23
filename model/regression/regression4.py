# encoding: utf-8

import numpy
import xlrd
import math
import xlwt
import spicy

excelName = ''      # excel name for result stored
permutation = 0.0       # order of data scrambling

# read and preprocess data
def readExcel (filename, ft1File, ft2File, ft3File, col_name_index = 0, by_index = 0) :
    data = xlrd.open_workbook(filename)
    table = data.sheets()[by_index]     # the excel data
    ncols = table.ncols     # the rows number of the data
    nrows = table.nrows     # the cols number of the data
    col_Name = table.row_values(col_name_index)     # the first row as an index

    # time interval
    t_1 = numpy.matrix(table.col_values(col_Name.index('t1')))
    t_1 = t_1[:, 1:].T
    t_1 = numpy.mat([int(x) for x in t_1]).T

    t_5 = numpy.matrix(table.col_values(col_Name.index('t5')))
    t_5 = t_5[:, 1:].T
    t_5 = numpy.mat([int(x) for x in t_5]).T

    t = t_5-t_1

    # predict ft1
    data = xlrd.open_workbook(ft1File)
    table = data.sheets()[by_index]
    ft1 = numpy.matrix(table.col_values(0)).T

    # predict ft2
    data = xlrd.open_workbook(ft2File)
    table = data.sheets()[by_index]
    ft2 = numpy.matrix(table.col_values(0)).T

    # predict ft3
    data = xlrd.open_workbook(ft3File)
    table = data.sheets()[by_index]
    ft3 = numpy.matrix(table.col_values(0)).T

    # column vectors of constant 1
    constant = numpy.mat(numpy.ones(ft1.shape[0])).T

    # data normalization
    t = normalization(t)

    # the data set
    xMat = numpy.hstack((constant, ft1, ft2, ft3))
    yMat = t

    global permutation
    yMat = yMat[permutation]
    yMat = yMat[int(nrows - nrows*0.1):, :]

    # disruption of data sequence
    permutation1 = numpy.random.permutation(xMat.shape[0])
    xMat = xMat[permutation1, :]
    yMat = yMat[permutation1]
    return xMat, yMat

# regression equation
def regression(xMat, yMat):
    yHat = lwlrTest (xMat, yMat)


# locally weighted linear regression
def lwlr(testPoint, xMat, yMat, k=0.08):        # k controls the decay rate
    m = numpy.shape(xMat)[0]
    weights = numpy.mat(numpy.eye((m)))         # weight matrix
    for j in range(m):
        diffMat = testPoint - xMat[j, :]
        weights[j, j] = math.exp(diffMat * diffMat.T / (-2.0*k**2))

    xTx = xMat.T * (weights * xMat)
    ws = xTx.I * (xMat.T * (weights * yMat))        # ws as wegression coefficient
    return testPoint * ws

def lwlrTest(xMat, yMat, k=0.08):
    [x, y] = numpy.shape(xMat)
    m = int(x * 0.1)
    xArr = xMat[:x - m, :]
    yArr = yMat[:x - m, :]
    TestArr = xMat[x - m:, :]
    yTestArr = yMat[x - m:, :]

    yHat = numpy.zeros(m)
    for i in range(m):
        yHat[i] = lwlr(TestArr[i], xArr, yArr, k)       # predictive value
        print(yHat[i]-yTestArr[i])

    yHat = numpy.mat(yHat).T
    data = numpy.hstack((yHat, yTestArr))

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
    min = numpy.min(data)
    max_log = math.log(float(max))
    data = numpy.mat([math.log(float(x))/max_log for x in data]).T
    return data

# get excel name for result stored
def getPermutation(num):
    global permutation
    permutation = num

# get order of data scrambling
def getExcelName(str):
    global excelName
    excelName = str