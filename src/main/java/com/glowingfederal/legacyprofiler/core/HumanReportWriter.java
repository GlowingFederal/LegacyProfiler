package com.glowingfederal.legacyprofiler.core; import java.io.*;
public final class HumanReportWriter implements ReportWriter { public String fileName(ProfileSession s){return "profile.log";} public void write(File f,ProfileSession s)throws IOException{ProfileWriter.writeHuman(f,s);} }
