set -e

is_empty(){
	local result=0
	for i in $*
	do
		if [ $i = "<EMPTY>" ]; then
			local result=1
			break
		fi
	done
	echo $result
}

create_temp_file(){
	mktemp
}

create_temp_dir(){
	mktemp -d
}

get_file_name(){
	echo $(basename $1)
}

get_file_name_without_extension(){
	local filename=`get_file_name $1`
	echo ${filename%.*}
}

get_file_name_extension(){
	local filename=`get_file_name $1`
	echo ${filename##*.}
}

get_path_to_file(){
	echo $(dirname $1)
}

combine_path(){
	echo "$1"|grep '/$'
	if [ $? -eq 0 ];then
	  echo "$1$2"
	else
		echo "$1/$2"
	fi
}
get_absolute_path(){
	local relative_path=$1
	if [ -d "$relative_path" ]
	then
	  	local absolute_path=`cd $relative_path; pwd`
	else
		local file_name=`get_file_name $relative_path`
		local dir_name=`get_path_to_file $relative_path`
		local dir_name=`cd $dir_name; pwd`
		local absolute_path=`combine_path $dir_name $file_name`
	fi
	echo $absolute_path
}

get_first_directory(){
	local parent_dir=$1
	for FILE in `find $parent_dir -mindepth 1 -maxdepth 1 -type d`
	do
		if test -d $FILE
		then
			echo $FILE
			break;
		fi
	done
}

get_first_file(){
	local parent_dir=$1
	for FILE in `find $parent_dir -mindepth 1 -maxdepth 1`
	do
		echo $FILE
		break;
	done
}

count_files_in_dir(){
	echo `ls -1 $1 | wc -l`
}

extract_zip_archive(){
	local archive_location=`get_absolute_path $1`
	local extract_location=`get_absolute_path $2`
	
	local current_dir=`pwd`
	mkdir -p $extract_location
	cd $extract_location
	unzip $archive_location
	cd $current_dir
}

apply_filters(){
	local filter_string=$1
    eval "declare -A filter_array="${2#*=}
	for key in "${!filter_array[@]}"; do 
		local r_key="\${$key}"
		filter_string=`echo ${filter_string//$r_key/${filter_array["$key"]}}`
	done
	echo $filter_string
}

extract_archive_to_location(){
	local ARCHIVE=$1
	local EXTRACT_TO=$2

	local TMP_PATH=`create_temp_dir`

	unique_dir=`get_file_name_without_extension $ARCHIVE`

	local TMP_EXTRACT_PATH=`combine_path $TMP_PATH $unique_dir`

	mkdir -p $TMP_EXTRACT_PATH

	something=`extract_zip_archive $ARCHIVE $TMP_EXTRACT_PATH`

	if [ `count_files_in_dir $TMP_EXTRACT_PATH` -gt 1 ]
	then
		local ARCHIVE_EXTRACTED_DIR=$TMP_EXTRACT_PATH
	else	
		local ARCHIVE_EXTRACTED_DIR=`get_first_directory $TMP_EXTRACT_PATH`
	fi

	mv $ARCHIVE_EXTRACTED_DIR $EXTRACT_TO

	archive_dir_name=`get_file_name $ARCHIVE_EXTRACTED_DIR`
	FINAL_RESTING_DIR=`combine_path $EXTRACT_TO $archive_dir_name`
	FINAL_RESTING_DIR=`get_absolute_path $FINAL_RESTING_DIR`
	rm -rf $TMP_PATH

	echo $FINAL_RESTING_DIR
}

print_help(){
	local script_name=$1
	echo "Usage: $script_name [OPTIONS] --tomcat=<TOMCAT_BIN> --airavata=<AIRAVATA_BIN> --axis2=<AXIS2_WAR> --jackrabbit=<JACKRABBIT_WAR> --properties=<DEPLOYEMENT_PROPERTIES>"
	echo "Setup airavata & jackrabbit on a tomcat server. Configure airavata services to use the jackrabbit in the tomcat."	
	echo
	echo "Mandatory arguments"
	echo "   --tomcat		Tomcat binary distribution or existing tomcat directory"
	echo "   --airavata		Airavata binary distribution"
	echo "   --axis2		axis2 webapp (eg: axis2.war)"
	echo "   --jackrabbit		jackrabbit webapp (eg: jackrabbit-webapp-2.4.0.war)"
	echo "   --properties		A properties file on how to configure/customize deployed webapps"
	echo
	echo "Optional arguments"
	echo "   -? --help		Print this help"
	echo "   --debug		Let there be more text :)"
	echo "   --base=PATH		Setup tomcat at the given location (ignored if --tomcat is a directory)"
	echo "   --start_tomcat	Once setup is done start the tomcat server"
	echo
	echo "Example:"
	echo "   \$ $script_name --tomcat=dists/apache-tomcat-7.0.26.zip --airavata=dists/apache-airavata-0.3-incubating-SNAPSHOT-bin.zip --axis2=downloaded_webapps/axis2.war --jackrabbit=downloaded_webapps/jackrabbit-webapp-2.4.0.war --properties=airavata-tomcat.properties"
}

print_debug(){
	if [ "$debug" = "debug_mode" ]; then
		echo $1
	fi
}

TOMCAT_BIN="<EMPTY>"
AIRAVATA_BIN="<EMPTY>"
AXIS2_WAR="<EMPTY>"
JR_WAR="<EMPTY>"
PROPERTIES_FILE="<EMPTY>"

TO_DIR="."
debug="normal_mode"
START_TOMCAT="no"
for i in $*
do
	case $i in
    	--tomcat=*)
			TOMCAT_BIN=`echo $i | sed 's/[-a-zA-Z0-9]*=//'`
			;;
    	--airavata=*)
			AIRAVATA_BIN=`echo $i | sed 's/[-a-zA-Z0-9]*=//'`
			;;
    	--axis2=*)
			AXIS2_WAR=`echo $i | sed 's/[-a-zA-Z0-9]*=//'`
			;;
    	--jackrabbit=*)
			JR_WAR=`echo $i | sed 's/[-a-zA-Z0-9]*=//'`
			;;
    	--properties=*)
			PROPERTIES_FILE=`echo $i | sed 's/[-a-zA-Z0-9]*=//'`
			;;
    	--base=*)
			TO_DIR=`echo $i | sed 's/[-a-zA-Z0-9]*=//'`
			;;
		--start_tomcat)
			START_TOMCAT="yes"
			;;
		--help)
			print_help $0
			exit 0
			;;
		-?)
			print_help $0
			exit 0
			;;
    	--debug)
			debug="debug_mode"
			;;
    	*)
        	echo "Invalid option specified: $i"
			print_help $0
			exit -1  
		;;
  	esac
done
if [ `is_empty $TOMCAT_BIN $AIRAVATA_BIN $AXIS2_WAR $JR_WAR $PROPERTIES_FILE` -eq 1 ]
then
	echo "Not enough parameters provided"
	print_help $0
	exit -1
fi

##################### Setup TOMCAT ########################
if [ -d $TOMCAT_BIN ];then
	TOMCAT_DIR=$TOMCAT_BIN
else
	echo -n "Extracting tomcat distribution..."
	TOMCAT_DIR=`extract_archive_to_location $TOMCAT_BIN $TO_DIR`
	echo "done."
fi
print_debug $TOMCAT_DIR

TOMCAT_WEBAPP_DIR=`combine_path $TOMCAT_DIR "webapps"`

##################### Setup Jackrabbit Webapp ########################
echo -n "Extracting jackrabbit webapp to tomcat..."
JR_WEBAPP_DIR=`extract_archive_to_location $JR_WAR $TOMCAT_WEBAPP_DIR`
echo "done."
print_debug $JR_WEBAPP_DIR
JR_WEB_CONTEXT=`get_file_name $JR_WEBAPP_DIR`

##################### Setup AXIS2 Webapp ########################
echo -n "Extracting axis2 webapp to tomcat..."
AXIS2_WEBAPP_DIR=`extract_archive_to_location $AXIS2_WAR $TOMCAT_WEBAPP_DIR`
echo "done."
print_debug $AXIS2_WEBAPP_DIR
axis2_service_dir=`combine_path $AXIS2_WEBAPP_DIR "WEB-INF/services"`
axis2_lib_dir=`combine_path $AXIS2_WEBAPP_DIR "WEB-INF/lib"`

##################### Setup Airavata in Axis2 Webapp ########################

TMP_DIR=`create_temp_dir`

#extract airavata
test=`extract_zip_archive $AIRAVATA_BIN $TMP_DIR`
airavata_tmp_dir=`get_first_directory $TMP_DIR`

##################### Read the properties file ########################

if [ ${IFS:-<NOT_SET>}="<NOT_SET>" ]; then
	old_ifs="<NOT_SET>"
else
	old_ifs=$IFS
fi

IFS=$'\n'
echo -n "Reading $PROPERTIES_FILE"
declare -A props
for line in `sed '/^\#/d' $PROPERTIES_FILE | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'`
do
	key=`echo $line | cut -d "=" -f1`
	value=`echo $line | cut -d "=" -f2-`
	props[$key]=$value
	echo -n "."
done
echo "done."

if [ $old_ifs="<NOT_SET>" ]; then
	unset IFS
else
	IFS=$old_ifs
fi
CURRENT_DIR=`pwd`

##################### Define the filters ########################

declare -A filter_data

filter_data["airavata.home"]=$airavata_tmp_dir
filter_data["tomcat.home"]=$TOMCAT_DIR
filter_data["axis2.home"]=$AXIS2_WEBAPP_DIR
filter_data["axis2.webapp.dirname"]=`get_file_name $AXIS2_WEBAPP_DIR`
filter_data["jackrabbit.web.context"]=$JR_WEB_CONTEXT
filter_data["jackrabbit.webapp.dirname"]=`get_file_name $JR_WEBAPP_DIR`



#first pass only to get the filtering values
for key in "${!props[@]}"; do 
	value=${props["$key"]}
	value=`apply_filters "$value" "$(declare -p filter_data)"`
	case $key in
		*.mkdir)
			;;
    	*.cp)
			;;
    	*.edit)
			;;
    	*.rm)
			;;
    	*.msg)
			;;
    	*)
        	filter_data["$key"]=$value
		;;
  	esac
done

for key in "${!props[@]}"; do 
	value=${props["$key"]}
	props[$key]=`apply_filters "$value" "$(declare -p filter_data)"`
	print_debug "key=$key value=${props[$key]}"
done

##################### Perform customizations in the property file ########################

#2nd pass to create directories
for key in "${!props[@]}"; do 
	value=${props["$key"]}
	case $key in
		*.mkdir)
			echo -n "Creating dir $value..."
			mkdir -p $value
			echo "done."
			;;
  	esac
done

#3rd pass copying files
for key in "${!props[@]}"; do 
	value=${props["$key"]}
	case $key in
    	*.cp)
			#do the copying
			from=`echo $value | awk -F'=>' '{ print $1 }'`
			to=`echo $value | awk -F'=>' '{ print $2 }'`
			`echo "$to" | grep -qE "/$"`
			if [ $? -eq 0 ];
			then
				# Ends with /, create the directory just incase if its not created
				mkdir -p $to
			fi
			if [[ $from == http* ]];
			then
				temp_dir=`create_temp_dir`
				cd $temp_dir
				if [ "$debug" = "debug_mode" ];then
					echo "Downloading $from..."
					wget -U "mozilla" $from
				else
					#do the downloading in silent
					echo -n "Downloading $from..."
					wget -q -U "mozilla" $from
					echo "done."
				fi
				from=`get_first_file $temp_dir`
				cp $from $to
				rm -rf $temp_dir
			else
				echo -n "Copying $from..."
				cp -r $from $to
				echo "done."
			fi
			;;
  	esac
done

#4th pass edit files
for key in "${!props[@]}"; do 
	value=${props["$key"]}
	case $key in
    	*.edit)
			filename=`echo $value | awk -F';' '{ print $1 }'`
			sed_char=`echo $value | awk -F';' '{ print $2 }'`
			modifier=`echo $value | awk -F';' '{ print $3 }'`
			search_str=`echo $modifier | awk -F'=>' '{ print $1 }'`
			replace_str=`echo $modifier | awk -F'=>' '{ print $2 }'`
			echo "Updating file $filename..."
			print_debug "    Searh   : $search_str"
			print_debug "    Replace : $replace_str ..."
			tmp_file_name=`create_temp_file`
			cat $filename | sed -r 's'$sed_char$search_str$sed_char$replace_str$sed_char'g' > $tmp_file_name
			cp $tmp_file_name $filename
			rm $tmp_file_name
			;;
  	esac
done

#5th pass remove files
for key in "${!props[@]}"; do 
	value=${props["$key"]}
	case $key in
    	*.rm)
			if [ -d $value ]; then
				echo -n "Removing directory $value..."
				rm -rf $value
			else
				echo -n "Removing file $value..."
				rm $value
			fi
			echo "done."
			;;
  	esac
done


#6th pass show messages
echo
echo "Generation Messages"
echo "==================="
for key in "${!props[@]}"; do 
	value=${props["$key"]}
	case $key in
    	*.msg)
			msg=`echo $value | awk -F'=>' '{ print $1 }'`
			msg_value=`echo $value | awk -F'=>' '{ print $2 }'`
			echo "$msg = $msg_value"
			;;
  	esac
done
echo

rm -rf $TMP_DIR

if [ "$START_TOMCAT" = "yes" ];
then
	cd `combine_path $TOMCAT_DIR "bin"`
	sh startup.sh
fi

cd $CURRENT_DIR


#rm -rf apache-tomcat-7.0.26; ./a.sh --tomcat=apache-tomcat-7.0.26.zip --airavata=apache-airavata-0.3-incubating-SNAPSHOT-bin.zip --axis2=axis2.war --jackrabbit=jackrabbit-webapp-2.4.0.war --properties=airavata-tomcat.properties

