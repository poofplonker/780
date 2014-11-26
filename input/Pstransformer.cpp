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
    const int CLASSES = 100;
    double a,b,d,e;
    int c;
    char waste;
    double first[CLASSES];
    double second[CLASSES];
    double third[CLASSES];
    double fourth[CLASSES];
    int counter[CLASSES];
    double waste2;
    for (int i = 0; i < CLASSES; i++) {
        counter[i] = 0;
    }
    while (cin >> waste2 >> waste>> a >> waste >> b >> waste >> d>> waste>>  e>> waste>> c) {
        first[c] += a;
        second[c] += b;
        third[c] += d;
        fourth[c] += e;
        counter[c]++;
        cout <<fixed << setprecision(3) << a << "," << b <<"," << d << "," << e <<",";
        if(c == 2 || (c >  27 && c < 44)){
            cout << '0' << endl;
        }else{
            cout << '1' << endl;
        }
        
    }
//    for(int i = 0; i < CLASSES; i++){
//        cout  << i << " "<<counter[i] <<" "<<first[i] /counter[i] << " "<<  second[i]/counter[i] << " "<<third[i]/counter[i] << " " << fourth[i]/counter[i] << endl;
//    }

}
