"""
  @package 
  @file eventFitsPlot.py
  @brief plot the optical digram
 
  @brief Created by:
  @author Jun Cheng (Purdue)
 
  @warning This code is not fully validated
  and not ready for full release.  Please
  treat results with caution.

Usage: python  
 
Notes: 1. 

"""

import numpy as np

def getShearPolar(data, sigma, ellipticity):
    sumX=sumY=0
    sumTotal = np.sum(data)
    for (i,j), value in np.ndenumerate(data):
        sumX+=i*value
        sumY+=j*value

    centerX= float(sumX)/sumTotal
    centerY= float(sumY)/sumTotal
    XX=YY=XY=0
    M00=M01=M10=M11=0
    G00=G01=G10=G11=0
    
    for (i,j), value in np.ndenumerate(data):
        dX = i-centerX
        dY = j-centerY
        dR = np.sqrt(dX**2+dY**2)
        var = sigma**2
        weight  = 1/(np.sqrt(2)*sigma)*np.exp(-dR**2/(2*var))
        weight1 = weight*(-dR/var)
        weight2 = weight*(-1/var)+weight1*(-dR/var)
        XX+=dX**2*value*weight
        YY+=dY**2*value*weight
        XY+=dX*dY*value*weight
        
        #eta0 = dX*dX - dY*dY
        #eta1 = 2*dX*dY
        #M00+= value*(weight+2*dDR**2*weight1/var+eta0*eta0*weight2/(var**2))
        #M01+= value*(eta0*eta1*weight2/(var**2))
        #M10+= value*(eta1*eta0*weight2/(var**2))
        #M11+= value*(weight+2*dDR**2*weight1/var+eta1*eta1*weight2/(var**2))
        #N0+= value*eta0*(2*weight/var+dR*dR*weight2/(var**2))
        
        P00 += value*(2*weight+4*weight1*dR**2+2*weight2*(dX**2-dY**2)**2- ellipticity0* (dX**2-dY**2)*(2*weight1+weight2*dR**2))
        P01 += value*(4*weight2*(dX**2-dY**2)*dX*dY- ellipticity0*(dX**2-dY**2)*(2*weight1+2*weight2*dR**2))
        P10 += value*(4*weight2*(dX**2-dY**2)*dX*dY- ellipticity1*(2*dX*dY)*(2*weight1+weight2*dR**2))
        P11 += value*(2*weight+4*weight1*dR**2+8*weight2*dX**2*dY**2- ellipticity1*(dX**2-dY**2)*(2*weight1+weight2*dR**2))

        G00 += value*(2*weight*dR**2+2*weight1*(dX**2-dY**2)**2) - ellipticity0*2*weight1*value*dR**2*(dX**2-dY**2)
        G01 += value*(4*weight1*(dX**2-dY**2)*dX*dY) - ellipticity0*2*weight1*value*dR**2*(dX**2-dY**2)
        G10 += value*(4*weight1*(dX**2-dY**2)*dX*dY) - ellipticity1*2*weight1*value*dR**2*(2*dX*dY)
        G11 += value*(2*weight*dR**2+8*weight1*dX**2*dY**2) -ellipticity1*2*weight1*value*dR**2*(2*dX*dY) 
    
    trace = XX + YY
    p00 = P00/trace
    p01 = P01/trace
    p10 = P10/trace
    p11 = P11/trace
    
    g00 = G00/trace- 2*ellipticity0*ellipticity0
    g01 = G01/trace- 2*ellipticity0*ellipticity1
    g10 = G10/trace- 2*ellipticity1*ellipticity0
    g00 = G11/trace- 2*ellipticity1*ellipticity1

    quadruPole = np.matrix([[XX,XY],[XY,YY]])
    smearPolar = np.matrix([[p00,p01],[p10,p11]])
    shearPolar = np.matrix([[g00,g01],[g10,g11]])
    return quadruPole, smearPolar, shearPolar

def getAnisotroy(smearPolar, starEllipticity):
    return np.dot(np.linalg.inv(smearPolar), starellipticity)


if __name__=="__main__":
    main()









