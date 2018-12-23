/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bheft;


public class TaskProcessor {

    int processor;
    //int service;
    double AST;
    double AFT;
    
    public TaskProcessor() {
        processor=-1;
    }

    public TaskProcessor(int processor,int service, double AST, double AFT) {
        this.processor = processor;
       // this.service=service;
        this.AST = AST;
        this.AFT = AFT;
    }

}
