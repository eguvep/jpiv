/* 	ReadLaVisionImage is based on the code example "Read_Examples.cpp" provided
	on the ftp server of LaVision (ftp://softver6:softver6@lavision.de/Tools).

	This code is used in conjunction with ReadIMX.cpp and ReadIM7.cpp from LaVision
	to load DaVis Images into the jpiv2 package.

	@author: p.vennemann@wbmt.tudelft.nl
*/

#include "ReadIMX.h"
#include "ReadIM7.h"

#include <jni.h>
#include "jpiv2_LaVisionImageLoader.h"


// Native method implementation to read DaVis Image files.
// This method is declared in the jpiv2 package as
// jpiv2.LaVisionImageLoader.loadImage(String pathname, int isIM7)
JNIEXPORT jfloatArray JNICALL Java_jpiv2_LaVisionImageLoader_loadImage
  (JNIEnv *env, jobject obj, jstring j_theFileName, jint j_isIM7)
{
	// conversion of java-type function parameters into c-types
	const char *theFileName = env->GetStringUTFChars(j_theFileName, 0);
	int isIM7 = j_isIM7;
	// reading the image data into a DaVis buffer
	BufferType myBuffer;
	if (isIM7)
		ReadIM7 ( theFileName, &myBuffer, NULL );
	else
		ReadIMX ( theFileName, &myBuffer, NULL );
	// the amount of data to read from the DaVis buffer
	int dataLength = myBuffer.nx * myBuffer.ny * myBuffer.nf + 5;
	// create a float array to hold the image data
	float *data = new float[dataLength];
	// store image size information in last four array elements
	data[dataLength-4] = myBuffer.nx;
	data[dataLength-3] = myBuffer.ny;
	data[dataLength-2] = myBuffer.nz;
	data[dataLength-1] = myBuffer.nf;
	// copy the image data from the DaVis Buffer into the float array
	if (myBuffer.isFloat) {
		for (int i=0; i<dataLength-4; i++) {
			data[i] = myBuffer.floatArray[i];
		}
	} else {
		for (int i=0; i<dataLength-4; i++) {
			data[i] = (float) myBuffer.wordArray[i];
		}
	}
	// copy data into java array to return
	jfloatArray j_data = env->NewFloatArray( (jint) dataLength );
	env->SetFloatArrayRegion(j_data, 0, dataLength, data);
	// free memory ('j_data' will be garbage collected after return)
	if (myBuffer.isFloat) free(myBuffer.floatArray);
	else free(myBuffer.wordArray);
	delete [] data;
	env->ReleaseStringUTFChars(j_theFileName, theFileName);
	return(j_data);
}
