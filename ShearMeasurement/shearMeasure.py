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
import os, sys
import subprocess
import matplotlib.pylab as plt
import numpy as np
#import pywcs
import pyfits
import pyshear


class ccdImage:
    def __init__(self, fileName):
        self.fullName = fileName
        self.fileName = fileName.split('.')
        self.catName = self.fileName[0]+".cat"
        self.aperFitsName = self.fileName[0]+"_aper.fits"
        self.nobackFitsName = self.fileName[0]+"_noback.fits"
        self.image = None
        self.piece = []

        self.min_starFlux = 0
        self.max_starFlux = 0
        self.min_starRadius = 0
        self.max_starRadius = 0

        self.min_galFlux = 0
        self.max_galFlux = 0
        self.min_galRadius = 0
        self.max_galRadius = 0

        self.numObjects   = 0
        self.objectID     = []
        self.xwin_image   = []
        self.ywin_image   = []
        self.fwhm         = []
        self.flux_radius  = []
        self.flux_auto    = []
        self.fluxerr_auto = []
        self.flag         = []
        self.class_star   = []
        self.alphaWIN     = []
        self.deltaWIN     = []

        self.typeFlag = []

        self.length_scale = []
        self.sigma = []

        self.shearPolar = []
        
    def runSex(self):
        cmdOpt1 = "-catalog_name " + self.catName
        cmdOpt2 = " -checkimage_type apertures,-background -checkimage_name " + self.aperFitsName + "," + self.nobackFitsName + " "
        cmd = "sex " + cmdOpt1 + cmdOpt2 + self.fullName
        subprocess.call(cmd, shell=True)
        
    def loadObjectParameters(self):
        f = open(self.catName,'r')
        for line in f.readlines():
            if line[0]!='#':
                temp = line.split()
                self.objectID.append(temp[0])
                self.xwin_image.append(temp[1])
                self.ywin_image.append(temp[2])
                self.fwhm.append(temp[3])
                self.flux_radius.append(temp[4])
                self.flux_auto.append(temp[5])
                self.fluxerr_auto.append(temp[6])
                self.flag.append(temp[7])
                self.class_star.append(temp[8])
                self.alphaWIN.append(temp[9])
                self.deltaWIN.append(temp[10])
        f.close()

    def printInfo(self):
        print "File Name: " + self.fullName
        print "Number of objects: " + str(self.numObjects)


    ###################################
    # Seperate stars and galaxies
    ###################################
    def sepStarGalaxy(self):
        for i in range(self.numObjects):
            if (self.flux_auto[i]>self.min_starFlux and self.flux_auto<self.max_starFlux) and (self.flux_radius[i]>self.min_starRadius and self.flux_radius[i]<self.max_starRadius):
                self.tpyeFlag.append('s')
            elif (self.flux_auto[i]>self.min_galFlux and self.flux_auto<self.max_galFlux) and (self.flux_radius[i]>self.min_galRadius and self.flux_radius[i]<self.max_galRadius):
                self.tpyeFlag.append('g')
            else:
                self.tpyeFlag.append('o')

        
    #def getSmearPolar(self):
        #dependin on self.alpha
    #    for i in
    # def getQuadruPole(self,sigma):
    #     f=pyfits.open(self.nobackFitsName)
    #     self.image =f[0].data
    #     #Take image into piece 

    #     winSizeX = 2*sigma
    #     winSizeY = 2*sigma
    #     centerX=[]
    #     centerY=[]
    #     for i in range(self.numObjects):
    #         self.piece[i].append(image[-winSizeX:winSizeX,-winSizeY:winSizeY])
    #         centerX.append(np.sum(self.piece[i],axis=1)/
    #     for i in range(-winSize, winSize):
    #         for j in range(-winSize, winSize):
    #             centerX=

    def plotStarGalaxy(self):
        plt.scatter(self.flux_auto, self.flux_radius)
        plt.xlabel("Flux_auto")
        plt.ylabel("Flus_radius")
        plt.savefig(self.fileName[0]+".eps",format='eps', dpi=1000)
        

    def procedure(self):
        self.runSex()
        self.loadObjectParameters()
        self.sepStarGalaxy()
        self.plotStarGalaxy()

def main():
    imageList=[]
    for i in range(0,len(sys.argv)-1):
        imageList.append(ccdImage(sys.argv[i+1]))
        imageList[i].procedure()
        imageList[i].printInfo()


if __name__=="__main__":
    main()









