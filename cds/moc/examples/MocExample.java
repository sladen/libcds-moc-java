// Copyright 2011 - UDS/CNRS
// The MOC API project is distributed under the terms
// of the GNU General Public License version 3.
//
//This file is part of MOC API java project.
//
//    MOC API java project is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, version 3 of the License.
//
//    MOC API java project is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    The GNU General Public License is available in COPYING file
//    along with MOC API java project.
//

package cds.moc.examples;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import cds.moc.Healpix;
import cds.moc.HealpixMoc;
import cds.moc.MocCell;

public class MocExample {
   
    static public void main(String[] args) throws Exception {
       
       try {
         // Creation by Stream
          String u = "http://alasky.unistra.fr/MocServer/query?ivorn=CDS/II/311/wise&get=moc";
          System.out.println("Loading this remote MOC: "+u+"...");
          URL url = new URL(u);
          BufferedInputStream bis = new BufferedInputStream(url.openStream(), 32*1024);
          HealpixMoc mocA = new HealpixMoc(bis);

          System.out.println("Moc sky coverage : "+pourcent(mocA.getCoverage()));
          System.out.println("Moc order        : "+mocA.getMocOrder());
          System.out.println("Moc resolution   : "+mocA.getAngularRes()*3600.+" arcsec");
          System.out.println("Number of cells  : "+mocA.getSize());
          System.out.println("Sorted           : "+mocA.isSorted());
          System.out.println("In memory        : ~"+mocA.getMem()+" bytes");
          System.out.println("Information      :"+
                " MOCTOOL="+mocA.getProperty("MOCTOOL")+
                " MOCTYPE="+mocA.getProperty("MOCTYPE")+
                " MOCID="+mocA.getProperty("MOCID")+
                " DATE="+mocA.getProperty("DATE")+
                " ORIGIN="+mocA.getProperty("ORIGIN")+
                " EXTNAME="+mocA.getProperty("EXTNAME"));
          System.out.print("Contents         : "); display("MocA",mocA);
//        for( MocCell item : moc ) System.out.println(" "+item);
          
          // Creation by list of cells (JSON format or basic ASCII format), write and reread it.
          HealpixMoc mocB = new HealpixMoc();
          mocB.add("{ \"3\":[2,53,55], \"4\":[20,21,22,25,28,30,50,60], \"5\":[456,567,836], \"9\":[123456] }");
//          mocB.add("3/2,53,55 4/20-22,25,28,30,50,60 5/456,567,836 9/123456");
          
          // Addition of some meta data
          mocB.setProperty("MOCTOOL","MyTool");          // Name of MOC tool generator 
          mocB.setProperty("MOCID","ivo://CDS/0001");    // MOC unique identifier
          mocB.setProperty("MOCORDER","12");             // MOC resolution (best cell order)
          mocB.write("/Moc.fits");
          mocB.read("/Moc.fits");
          System.out.print("\nAnother Moc created by string: "); display("MocB",mocB);
          
          // Intersection, union, clone
          HealpixMoc clone = (HealpixMoc)mocA.clone(); 
          HealpixMoc union = mocA.union(mocB);
          HealpixMoc inter = mocA.intersection(mocB);
          System.out.println("\nMocA coverage      : "+pourcent(mocA.getCoverage()));
          System.out.println("MocB coverage      : "+pourcent(mocB.getCoverage()));
          System.out.println("Moc union coverage : "+pourcent(union.getCoverage()));
          System.out.println("Moc inter coverage : "+pourcent(inter.getCoverage()));
          
          // Writing in FITS format
          File f;
          f = new File("Moc.fits");
          System.out.println("\nWriting MocA in FITS file "+f.getAbsolutePath());
          OutputStream outFits = (new FileOutputStream( f ));
          mocA.writeFits(outFits);
          outFits.close();
          
          // Writing in JSON format
          f = new File("Moc.txt");
          System.out.println("Writing MocA in JSON file "+f.getAbsolutePath());
          OutputStream outJson = (new FileOutputStream( f ));
          mocA.writeJSON(outJson);
          outJson.close();
          
          // HEALPix cell queries
          int order, npix;
          order=5; npix=849;
          System.out.println("\nHEALPix cell "+order+"/"+npix+" => inside mocA : "+mocA.isIntersecting(order, npix) );
          order=7; npix=14103;
          System.out.println("HEALPix cell "+order+"/"+npix+" => inside mocA : "+mocA.isIntersecting(order, npix) );
          System.out.println("MocA => intersects mocB : "+mocA.isIntersecting(mocB) );
          
          
          // Coordinate queries
          Healpix hpx = new Healpix();
          double al,del,radius;
          al = 095.73267; del = 69.55885;
          System.out.println("Coordinate ("+al+","+del+") => inside MocA : "+mocA.contains(hpx,al,del));
          al = 095.60671; del = 69.57092;
          System.out.println("Coordinate ("+al+","+del+") => inside MocA : "+mocA.contains(hpx,al,del));
          
          
          // circle queries
          al = 282.81215; del =  -70.20608; radius = 30/60.;
          HealpixMoc circle = mocA.queryDisc(hpx, al, del, radius);
          display("MocA intersection with circle("+al+","+del+","+radius+")", circle);
          circle.setMocOrder(6);
          display("Same result for limit order 6", circle);
          
          
      } catch( Exception e ) {
         e.printStackTrace();
      }

    }
    
    static String pourcent(double d) { return (int)(1000*d)/100.+"%"; }

    // Just for displaying a few cells
    static public void display(String title,HealpixMoc moc) {
       System.out.print(title+":");
       int i=0;
       int oOrder=-1;
       for( MocCell item : moc ) {
          if( oOrder!=item.order ) {
             System.out.print("\n   "+item.order+"/");
             oOrder=item.order;
             i=0;
          }

          if( i!=0 && i<20) System.out.print(",");
          if( i<20 )  System.out.print(item.npix);
          if( i==21 ) System.out.print("...");
          i++;
       }
       System.out.println();
    }
}
