#! /usr/bin/perl
  #use warnings;
  #use strict;

  if(@ARGV < 6)
  {
	print "\n\n\n";
	print "========================================================================================================================\n";
        print "==== Please Input serverName and sequence and queueDelimiter and osgiConsole Port and Min HeapSize and Max Hip Size!!===\n";
	print "========================================================================================================================\n";
	print "\n\n\n";
        exit;
  }
 
  my $serverName     = $ARGV[0];
  my $sequence       = $ARGV[1];
  my $queueDelimiter = $ARGV[2];
  my $consolePort    = $ARGV[3];
  my $xms            = $ARGV[4];
  my $xmx            = $ARGV[5];
  my $cim            = $ARGV[6];
  my $shop           = $ARGV[7];
  my $dqName         = $ARGV[8];
  my $sw             = $ARGV[9];
  my $ww             = $ARGV[10];
  my $wt             = $ARGV[11];
  my $tempArgument   = $ARGV[12];

  my $factory = "F1";
  
  if($sw eq "") {
	$sw = "10";
	}
  if($ww eq "10"){
	$ww = "1";
  }
  if($wt eq ""){
	$wt = "1";
  }
  print "Input Parameter is $serverName  $sequence  $queueDelimiter  $consolePort  $xms  $xmx  $cim  $shop  $dqName \$schedulerWeight \$workerWeight \$workerTasks  $tempArgument\n";

  #=======================================
  #1. find ProcessId
  #=======================================
  my $findedProcessId = findProcessId($serverName, $sequence, $shop);
   
  print "find ProcessId=$findedProcessId\n";
  if($findedProcessId != -1)
  {
  	print "Already Server is running and Please stopServer and execute this script\n";
  	exit;
  }
  

  #if($serverName eq "CNMsvr" or $serverName eq "PEMsvr" or $serverName eq "TEMsvr" or $serverName eq "QRYsvr")
  #{
	my $location = "TRULY";
        my $source="*";
  #}
  #else
  #{
  #	$location = "TRULY";
  #      $source="*";
  #}

  my $JAVA_HOME=$ENV{"JAVA_HOME"}; 
  my $TIBCO_HOME=$ENV{"TIBCO_HOME"};
  
  print "$JAVA_HOME.......$TIBCO_HOME \n";
 
  system("export SHLIB_PATH=$TIBCO_HOME/lib");
  
  my $HOME  = $ENV{"MES_SOLUTION_ROOT"};
  my $SHLIB_PATH = $ENV{"SHLIB_PATH"}; 
  
  #$LD_LIBRARY_PATH = $ENV{"LD_LIBRARY_PATH"};
  
  my $DMODE=$ENV{"DMODE"}; 

  my $cfg         = "$HOME/cfg";
  my $config      = "$HOME/config";
  my $lib         = "$HOME/lib";
  my $log         = "$HOME/log";
  my $gclog       = "$HOME/log/gc";
  my $bpels       = "$HOME/bpels";
  my $rundir      = "$HOME/rundir";
  my $serverdir   = "$rundir/$serverName";
  my $privateHome = "$rundir/$serverName/$serverName$sequence"; 
  my $osgi        = "$privateHome/lib/org.eclipse.osgi_3.6.1.R36x_v20100806.jar"; 
  my $logFile     = "$HOME/config/log/log4j.xml";
  
  my $dMode = "$DMODE";

  #create directory if not exists
  if ( !-d $log )
  {
	system("mkdir $log");
  }
  
  if ( !-d $gclog )
  {
	system("mkdir $gclog");
  }
  
  if ( !-d $rundir )
  {
	system("mkdir $rundir");
  }
  
  if ( !-d $serverdir )
  {
	system("mkdir $serverdir");
  }
  
  if ( !-d $privateHome )
  {
	system("mkdir $privateHome");
  }
  
  system("cp $cfg/$serverName/config.ini $privateHome/");

  if ( !-d $privateHome."/lib" )
  {
	system("mkdir $privateHome/lib");
  }
  else
  {
 	system("rm -rf $privateHome/lib/*");
  }
  system("cp -rf $lib $privateHome");

  my $executeScript = "";
  if($tempArgument eq "")
  {
    $executeScript = "$JAVA_HOME/bin/java -jar -DSeq=$serverName$sequence -Dprocess=$consolePort -Dsvr=$serverName -Dshop=$shop -Dlocation=$location -Dfactory=$factory -Dcim=$cim -Dmode=$dMode -Dsource=$source -Dqueue=$queueDelimiter -Ddq=$dqName -DschedulerWeight=$sw -DworkerWeight=$ww -DworkerTasks=$wt -Dgreenframe.service.lookup.timeout=500 -Xms$xms -Xmx$xmx -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:ParallelGCThreads=6 -XX:+UseGetTimeOfDay -Xverbosegc:file=../log/gc/$serverName.gc -DLogDir=$log -Dport=$consolePort -Dfile.encoding=utf-8 -Dlog4j.configuration=file://$logFile -Dlog4j.watchDelay=20000 -DconfigRootPath=$config $osgi -console $consolePort -configuration file:$privateHome -framework file:$lib -clean &";
  }
  else
  {
    $executeScript = "$JAVA_HOME/bin/java -jar -DSeq=$serverName$sequence -Dprocess=$consolePort -Dsvr=$serverName -Dshop=$shop -Dlocation=$location -Dfactory=$factory -Dcim=$cim -Dmode=$dMode -Dsource=$source -Dqueue=$queueDelimiter -Ddq=$dqName -DschedulerWeight=$sw -DworkerWeight=$ww -DworkerTasks=$wt -Dgreenframe.service.lookup.timeout=500 -Xms$xms -Xmx$xmx -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:ParallelGCThreads=6 -XX:+UseGetTimeOfDay -Xverbosegc:file=../log/gc/$serverName.gc -DLogDir=$log -Dport=$consolePort -Dfile.encoding=utf-8 -Dlog4j.configuration=file://$logFile -Dlog4j.watchDelay=20000 -DconfigRootPath=$config -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=$tempArgument,suspend=y $osgi -console -configuration file:$privateHome -framework file:$lib -clean ";
  } 
  print $executeScript."\n";
  
  system($executeScript);



  sub findProcessId
  {
        my $serverName = $_[0];
        my $sequence   = $_[1];
        my $shop       = $_[2];
        my $returnProcessId = -1;

        foreach $_(`ps -eo pid,args`)
        {
                #print "$_\n";

                $_ =~ s/ +/ /g;   # remove duplicated white space
                $_ =~ s/^ | $//g; # remove front white space
                #print "$_\n";
                my @prcInfoList = split(/ /);
                my $processId = $prcInfoList[0];
                
		#print "ProcessID=$processId\n";
                #$processName = "";
                
		my $tempProcNamePre = "";
                my $tempProcNameBack = "";
                my $tempFactory = "";
                
                foreach(@prcInfoList)
                {
		        my @tempSplit = "";

                        if($_ =~/-Dsvr/)
                        {
                                @tempSplit = split(/=/);
                                $tempProcNamePre = $tempSplit[1];
                        }

                        if($_ =~/-DSeq/)
                        {
                                @tempSplit = split(/=/);
                                $tempProcNameBack = $tempSplit[1];
                        }

                        if($_ =~/-Dshop/)
                        {
                                @tempSplit = split(/=/);
                                $tempFactory = $tempSplit[1];
                        }
                }
                if($serverName eq $tempProcNamePre && "$serverName$sequence" eq $tempProcNameBack && $shop eq $tempFactory)
                {
		print "$serverName eq $tempProcNamePre && $sequence eq $tempProcNameBack && $shop eq $tempFactory";
                        $returnProcessId = $processId;
                        last;
                }
        }
        print "************ PROCESSID=$returnProcessId ************\n";
        return $returnProcessId;
        
  }


