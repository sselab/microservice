# encoding: utf-8
import test1
import test2
import test3
import numpy
import xlwt
import xlrd
import test4

filename1 = 'batch1.xls'
filename2 = 'batch2.xls'

if __name__ == '__main__':

    # order of data scrambling
    permutation = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

    workbook = xlwt.Workbook()
    sheet = workbook.add_sheet('permutation', cell_overwrite_ok=True)

    # generate 10 random sequences
    for i in range(10):
        permutation[i] = numpy.random.permutation(12500)        # generate random number
        for j in range(12500):
            sheet.write(j, i, permutation[i][j])        # save permutation
    workbook.save('permutation.xls')

    # read permutation from file
    data = xlrd.open_workbook('permutation.xls')
    table = data.sheets()[0]
    for i in range(table.ncols):
        permutation[i] = table.col_values(i)
        permutation[i] = [int(x) for x in permutation[i]]

    # make regression prediction of t1 in the file 'batch1.xls'
    for i in range(10):
        excelName = 'test1_Data'+str(i+1)+'_1.xls'
        regression1.getExcelName(excelName)
        regression1.getPermutation(permutation[i])
        xMat, yMat = regression1.readExcel(filename1)
        regression1.regression(xMat, yMat)

    # make regression prediction of t1 in the file 'batch2.xls'
    for i in range(10):
        excelName = 'test1_Data'+str(i+1)+'_2.xls'
        regression1.getExcelName(excelName)
        regression1.getPermutation(permutation[i])
        xMat, yMat = regression1.readExcel(filename2)
        regression1.regression(xMat, yMat)

    # make regression prediction of t2 in the file 'batch1.xls'
    for i in range(10):
        excelName = 'test2_Data'+str(i+1)+'_1.xls'
        regression2.getExcelName(excelName)
        regression2.getPermutation(permutation[i])
        xMat, yMat = regression2.readExcel(filename1)
        regression2.regression(xMat, yMat)

    # make regression prediction of t2 in the file 'batch2.xls'
    for i in range(10):
        excelName = 'test2_Data'+str(i+1)+'_2.xls'
        regression2.getExcelName(excelName)
        regression2.getPermutation(permutation[i])
        xMat, yMat = regression2.readExcel(filename2)
        regression2.regression(xMat, yMat)

    # make regression prediction of t3 in the file 'batch1.xls'
    for i in range(10):
        excelName = 'test3_Data'+str(i+1)+'_1.xls'
        regression3.getExcelName(excelName)
        regression3.getPermutation(permutation[i])
        xMat, yMat = regression3.readExcel(filename1)
        regression3.regression(xMat, yMat)

    # make regression prediction of t3 in the file 'batch2.xls'
    for i in range(10):
        excelName = 'test3_Data'+str(i+1)+'_2.xls'
        regression3.getExcelName(excelName)
        regression3.getPermutation(permutation[i])
        xMat, yMat = regression3.readExcel(filename2)
        regression3.regression(xMat, yMat)

    # predict the total time used The predicted value of t1、t2 and t3 in the file 'batch1.xls'
    for i in range(10):
        excelName = 'testTotal'+str(i+1)+'_1.xls'
        ft1File = 'test1_Data'+str(i+1)+'_1.xls'
        ft2File = 'test2_Data'+str(i+1)+'_1.xls'
        ft3File = 'test3_Data'+str(i+1)+'_1.xls'
        regression4.getExcelName(excelName)
        regression4.getPermutation(permutation[i])
        xMat, yMat = regression4.readExcel(filename1, ft1File, ft2File, ft3File)
        regression4.regression(xMat, yMat)

    # predict the total time used The predicted value of t1、t2 and t3 in the file 'batch2.xls'
    for i in range(10):
        excelName = 'testTotal'+str(i+1)+'_2.xls'
        ft1File = 'test1_Data'+str(i+1)+'_2.xls'
        ft2File = 'test2_Data'+str(i+1)+'_2.xls'
        ft3File = 'test3_Data'+str(i+1)+'_2.xls'
        regression4.getExcelName(excelName)
        regression4.getPermutation(permutation[i])
        xMat, yMat = regression4.readExcel(filename2, ft1File, ft2File, ft3File)
        regression4.regression(xMat, yMat)
