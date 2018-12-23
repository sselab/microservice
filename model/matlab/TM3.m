clear
clc
close all;  
set(0,'defaultfigurecolor','w')


A1(:,:)=xlsread('Xdata1.xls');
Y1 = A1(16671:25000,2);
X1 = A1(16671:25000,1);
figure;
subplot(1,4,1)
plot(X1,Y1,'.');
hold on
[P1,S1]=polyfit(X1,Y1,1);
[Y1,~]=polyconf(P1,X1,S1);
x1 = 0.5:0.00001:1.05;
f1=polyval(P1,x1);
B1=plot(X1,Y1,x1,f1,'-');
set(B1,'LineWidth',2.5); 
xlabel('Tr-prediction');
ylabel('Tr-real');



A2(:,:)=xlsread('Xdata2.xls');
Y2 = A2(16671:25000,2);
X2 = A2(16671:25000,1);
subplot(1,4,2)
plot(X2,Y2,'.');
hold on
[P2,S2]=polyfit(X2,Y2,1);
[Y2,~]=polyconf(P2,X2,S2);
x1 = 0.5:0.00001:1.05;
f2=polyval(P2,x1);
B2=plot(X2,Y2,x1,f2,'-');
axis([0.5,1.2,-inf,inf])
set(B2,'LineWidth',2.5);
xlabel('Tb-prediction');
ylabel('Tb-real');


A3(:,:)=xlsread('Xdata3.xls');
Y3 = A3(16671:25000,2);
X3 = A3(16671:25000,1);
subplot(1,4,3)
plot(X3,Y3,'.');
hold on
[P3,S3]=polyfit(	X3,Y3,1);
[Y3,~]=polyconf(P3,X3,S3);
x1 = 0.5:0.00001:1.05;
f3=polyval(P3,x1);
B3=plot(X3,Y3,x1,f3,'-');
axis([0.5,1.2,-inf,inf])
set(B3,'LineWidth',2.5);
xlabel('Td-prediction');
ylabel('Td-real');

A4(:,:)=xlsread('TotalXdata.xls');
Y4 = A4(1668:2500,2);
X4 = A4(1668:2500,1);
subplot(1,4,4)
plot(X4,Y4,'.');
hold on
[P4,S4]=polyfit(	X4,Y4,1);
[Y4,~]=polyconf(P4,X4,S4);
x1 = 0.5:0.00001:1.05;
f4=polyval(P4,x1);
B4=plot(X4,Y4,x1,f4,'-');
axis([0.5,1.2,-inf,inf])
set(B4,'LineWidth',2.5);
xlabel('totalTime-prediction');
ylabel('totalTime-real');


suptitle('Prediction of processing time for FB on TM3');