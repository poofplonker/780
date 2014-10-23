//
//  Pstransformer.cpp
//  PC
//
//  Created by Cinnamon Scudworth on 23/10/14.
//  Copyright (c) 2014 Cinnamon Scudworth. All rights reserved.
//

#include <iostream>
#include <iomanip>
using namespace std;
int main(void){
    double a,b;
    int c;
    char waste;
    double first[25];
    double second[25];
    int counter = 0;
    while (cin >> a >> waste >> b  >> waste >> c) {
        first[c] += a;
        second[c] += b;
        counter++;
        cout << fixed <<setprecision(1) << a << "," << b <<",";
        if(c < 7){
            cout << '0' << endl;
        }else{
            cout << '1' << endl;
        }
        
    }
//    for(int i = 0; i < 24; i++){
//        cout  << i << " "<<first[i] /counter << " "<<  second[i]/counter << endl;
//    }

}
