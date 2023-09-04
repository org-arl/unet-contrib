/****************************************************************************
wav_file.h
Copyright (C) 2000 to 2022 Sigma Numerix Ltd. All rights reserved.
---------------------------------------------------------------------------
Description : .WAV file I/O routines

    .WAV file header format :

    4 bytes     "RIFF"      ID for a RIFF file
    4 bytes     xxxx        Length of waveform (Bytes)
    8 bytes     "WAVEfmt "  ID for a .WAV file
    4 bytes     xxxx        Size of format section (Bytes)
    2 bytes     xx          Format - "1" indicates PCM data
    2 bytes     xx          Number of channels
    4 bytes     xxxx        Sample rate (Hz)
    4 bytes     xxxx        Data rate (Bytes per second)
    2 bytes     xx          Bytes per sample (rounded up)
    2 bytes     xx          Bits per sample
    /additional informaiton that may be used in more complex wave files/
    4 bytes     "data"      ID for data section
    4 bytes     xxxx        Length of data section (Bytes)

Note : .wav files store 16 bit data in signed format and 8 bit data unsigned

Retreived from https://github.com/Numerix-DSP/wav_file/blob/main/include/wav_file.h

****************************************************************************/

#ifndef __WAV_FILE_LIBRARY__                // If NUMERIX_HOST_LIBRARY not defined then declare it
#define __WAV_FILE_LIBRARY__    1

#include <stdio.h>
#include <string.h>
#include <math.h>

#ifdef __cplusplus                          // Declaration for C++ program calls
extern "C"
{
#endif


// Functions for reading and writing .wav files
typedef struct {
    int     SampleRate;
    int     NumberOfSamples;                // Per channel
    short   NumberOfChannels;
    short   WordLength;
    short   BytesPerSample;
    short   DataFormat;
} WAV_FILE_INFO;

int wav_read_data (double *, FILE *, const WAV_FILE_INFO, const int);
void wav_write_data (const double *, FILE *, const WAV_FILE_INFO, const int);
short wav_read_word (FILE *);
int wav_read_int (FILE *);
void wav_write_word (const short, FILE *);
void wav_write_int (const int, FILE *);
WAV_FILE_INFO wav_read_header (FILE *);
void wav_write_header (FILE *, const WAV_FILE_INFO);
void wav_display_info (const WAV_FILE_INFO);
WAV_FILE_INFO wav_set_info (const int, const int, const short, const short, const short, const short);
int wav_file_length (const char *);
WAV_FILE_INFO wav_read_file (double *, const char *);
int wav_write_file (const double *, const char *, const WAV_FILE_INFO, const int);
int wav_write_file_scaled (const double *, const char *, const WAV_FILE_INFO, const int);



int wav_read_long (FILE *FPtr);
void wav_write_long (const int LongWord, FILE *FPtr);


/**/
/********************************************************
* Function: wav_read_data
*
* Parameters:
*   double *pData,                   - Output array pointer
*   FILE *FPtr,                     - File pointer
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int SampleCount    - Number of samples read
*   Returns SampleCount = 0 on error
*
* Description: Read a array of data from a .WAV file.
*
********************************************************/

int wav_read_data (double *pData,
    FILE *FPtr,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)

{
    int    i;
    int    SampleCount = 0;
    int    Word;
    unsigned char   Char;


    for (i = 0; i < BufLen; i++) {                  // Read the data
        if (WavInfo.WordLength == 32) {             // Read 32 bit data
            Word = (int) wav_read_long (FPtr);

            if (feof (FPtr)) {                      // Check end of file error
                *pData++ = 0.0;
            }

            else {
                *pData++ = (double) Word;
                SampleCount++;
            }
        }

        if (WavInfo.WordLength == 16) {             // Read 16 bit data
            Word = (int) wav_read_word (FPtr);

            if (feof (FPtr)) {                      // Check end of file error
                *pData++ = 0.0;
            }

            else {
                *pData++ = (double) Word;
                SampleCount++;
            }
        }

        else if (WavInfo.WordLength == 8) {         // Read 8 bit data
            Char = (unsigned char)getc (FPtr);

            if (feof (FPtr)) {                      // Check end of file error
                *pData++ = 0.0;
            }

            else {
                *pData++ = ((double) Char) - 128.0;
                SampleCount++;
            }
        }

        else {
            printf ("Input wave file word length format error\n");
            SampleCount = 0;
            return (SampleCount);
        }
    }

    return (SampleCount);

}       // End of wav_read_data ()




/**/
/********************************************************
* Function: wav_write_data
*
* Parameters:
*   const double *pData,             - Buffer pointer
*   FILE *FPtr,                     - File pointer
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int sample_count   - Number of samples written
*
* Description: Write a array of data to a .WAV file.
*
********************************************************/

void wav_write_data (const double *pData,
    FILE *FPtr,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)

{
    int    i;

    for(i = 0; i < BufLen; i++) {                   // Write the data
        if (WavInfo.WordLength == 32) {             // Write 32 bit data
            wav_write_long ((int)*(pData + i), FPtr);
        }

        else if (WavInfo.WordLength == 16) {        // Write 16 bit data
            wav_write_word ((short)*(pData + i), FPtr);
        }

        else if (WavInfo.WordLength == 8) {         // Write 8 bit data
            putc (((short)(*(pData + i) + 128.0)) & 0x0ff, FPtr);
        }

        else {
            printf ("Input wave file word length format error\n");
            return;
        }
    }
}       // End of wav_write_data ()



/**/
/********************************************************
* Function: wav_read_data16
*
* Parameters:
*   double *pData,                   - Output array pointer
*   FILE *FPtr,                     - File pointer
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int SampleCount    - Number of samples read
*   Returns SampleCount = 0 on error
*
* Description: Read a array of 16 bit data from a .WAV file.
*
********************************************************/

int wav_read_data16 (short *pData,
    FILE *FPtr,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)

{
    int    i;
    int    SampleCount = 0;
    short   Word;
    char    Char;


    for (i = 0; i < BufLen; i++) {                  // Read the data
        if (WavInfo.WordLength == 16) {             // Read 16 bit data
            Word = wav_read_word (FPtr);

            if (feof (FPtr)) {                      // Check end of file error
                *pData++ = (short) 0;
            }

            else {
                *pData++ = (short) Word;
                SampleCount++;
            }
        }

        else if (WavInfo.WordLength == 8) {         // Read 8 bit data
            Char = (char)getc (FPtr);

            if (feof (FPtr)) {                      // Check end of file error
                *pData++ = (short) 0;
            }

            else {
                *pData++ = (short) Char;
                SampleCount++;
            }
        }

        else {
            printf ("Input wave file word length format error\n");
            SampleCount = 0;
            return (SampleCount);
        }
    }

    return (SampleCount);

}       // End of wav_read_data16 ()




/**/
/********************************************************
* Function: wav_write_data16
*
* Parameters:
*   const double *pData,             - Buffer pointer
*   FILE *FPtr,                     - File pointer
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int sample_count   - Number of samples written
*
* Description: Write a array of 16 bit data to a .WAV file.
*
********************************************************/

void wav_write_data16 (const short *pData,
    FILE *FPtr,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)

{
    int    i;

    for(i = 0; i < BufLen; i++) {                   // Write the data
        if (WavInfo.WordLength == 16) {             // Write 16 bit data
            wav_write_word ((short)*(pData + i), FPtr);
        }

        else if (WavInfo.WordLength == 8) {         // Write 8 bit data
            putc (((short)*(pData + i)) & 0x0ff, FPtr);
        }

        else {
            printf ("Input wave file word length format error\n");
            return;
        }
    }
}       // End of wav_write_data16 ()



/**/
/********************************************************
* Function: wav_read_data32
*
* Parameters:
*   double *pData,                   - Output array pointer
*   FILE *FPtr,                     - File pointer
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int SampleCount    - Number of samples read
*   Returns SampleCount = 0 on error
*
* Description: Read a array of 32 bit data from a .WAV file.
*
********************************************************/

int wav_read_data32 (int *pData,
    FILE *FPtr,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)

{
    int    i;
    int    SampleCount = 0;
    short   Word;
    char    Char;


    for (i = 0; i < BufLen; i++) {                  // Read the data
        if (WavInfo.WordLength == 16) {             // Read 16 bit data
            Word = wav_read_word (FPtr);

            if (feof (FPtr)) {                      // Check end of file error
                *pData++ = (int) 0;
            }

            else {
                *pData++ = (int) Word;
                SampleCount++;
            }
        }

        else if (WavInfo.WordLength == 8) {         // Read 8 bit data
            Char = (char)getc (FPtr);

            if (feof (FPtr)) {                      // Check end of file error
                *pData++ = (int) 0;
            }

            else {
                *pData++ = (int) Char;
                SampleCount++;
            }
        }

        else {
            printf ("Input wave file word length format error\n");
            SampleCount = 0;
            return (SampleCount);
        }
    }

    return (SampleCount);

}       // End of wav_read_data32 ()




/**/
/********************************************************
* Function: wav_write_data32
*
* Parameters:
*   const double *pData,             - Buffer pointer
*   FILE *FPtr,                     - File pointer
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int sample_count   - Number of samples written
*
* Description: Write a array of 32 bit data to a .WAV file.
*
********************************************************/

void wav_write_data32 (const int *pData,
    FILE *FPtr,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)

{
    int    i;

    for(i = 0; i < BufLen; i++) {                   // Write the data
        if (WavInfo.WordLength == 16) {             // Write 16 bit data
            wav_write_word ((short)*(pData + i), FPtr);
        }

        else if (WavInfo.WordLength == 8) {         // Write 8 bit data
            putc (((short)*(pData + i)) & 0x0ff, FPtr);
        }

        else {
            printf ("Input wave file word length format error\n");
            return;
        }
    }
}       // End of wav_write_data32 ()



/**/
/********************************************************
* Function: wav_read_word
*
* Parameters:
*   FILE    *FPtr   - File pointer
*
* Return value:
*   short Word  - Word read from file
*
* Description: Read a 16 bit of data from a .WAV file.
*
********************************************************/

short wav_read_word (FILE *FPtr)

{
    short       Word;

    Word = (short)(getc (FPtr));
    Word |= ((short)(getc (FPtr))) << 8;

    return (Word);

}       // End of wav_read_word ()




/**/
/********************************************************
* Function: wav_read_long
*
* Parameters:
*   FILE *FPtr  - File pointer
*
* Return value:
*   int LongWord       - Long word read from file
*
* Description: Read a 32 bit of data from a .WAV file.
*
********************************************************/

int wav_read_long (FILE *FPtr)

{
    int    LongWord;

    LongWord = (int)(getc (FPtr));
    LongWord |= ((int)(getc (FPtr))) << 8;
    LongWord |= ((int)(getc (FPtr))) << 16;
    LongWord |= ((int)(getc (FPtr))) << 24;

    return (LongWord);

}       // End of wav_read_long ()




/**/
/********************************************************
* Function: wav_write_word
*
* Parameters:
*   const short Word,   - Word to write to file
*   FILE *FPtr          - File pointer
*
* Return value:
*   void
*
* Description: Write a 16 bit of data to a .WAV file.
*
********************************************************/

void wav_write_word (const short Word,
    FILE *FPtr)

{
    putc (Word & 0x0ff, FPtr);
    putc ((Word >> 8) & 0x0ff, FPtr);

}       // End of wav_write_word ()




/**/
/********************************************************
* Function: wav_write_long
*
* Parameters:
*   const short LongWord,   - Long word to write to file
*   FILE *FPtr              - File pointer
*
* Return value:
*   void
*
* Description: Write a 32 bit of data to a .WAV file.
*
********************************************************/

void wav_write_long (const int LongWord,
    FILE *FPtr)

{
    putc ((short)(LongWord & 0x0ff), FPtr);
    putc ((short)((LongWord >> 8) & 0x0ff), FPtr);
    putc ((short)((LongWord >> 16) & 0x0ff), FPtr);
    putc ((short)((LongWord >> 24) & 0x0ff), FPtr);

}       // End of wav_write_long ()




/**/
/********************************************************
* Function: wav_read_header
*
* Parameters:
*   FILE *FPtr      - File pointer
*
* Return value:
*   WAV_FILE_INFO WavInfo   - WAV file info struct
*   Returns WavInfo.NumberOfSamples = 0 on error
*
* Description: Read a .WAV file header section.
*
********************************************************/

WAV_FILE_INFO wav_read_header (FILE *FPtr)

{
    int             LongWord;
    short           Word;
    char            String[10];
    WAV_FILE_INFO   WavInfo;
    short           DataSectionFound = 0;

    WavInfo.NumberOfChannels = 0;                   // Deafult error condition

    rewind (FPtr);

        // 4 bytes      "RIFF"      ID for a RIFF file
    fread (String, 1, 4, FPtr); *(String+4) = 0;
    if (strcmp (String, "RIFF") != 0) {
//      printf ("File is not RIFF format\n");*/
        fclose (FPtr);
        WavInfo.NumberOfSamples = 0;
        return (WavInfo);
    }

        // 4 bytes      xxxx        Length of waveform (Bytes)
    LongWord = wav_read_long (FPtr);

        // 8 bytes      "WAVEfmt "  ID for a .WAV file
    fread (String, 1, 8, FPtr); *(String+8) = 0;
    if (strcmp (String, "WAVEfmt ") != 0) {
//      printf ("File is not WAVEfmt format\n");*/
        fclose (FPtr);
        WavInfo.NumberOfSamples = 0;
        return (WavInfo);
    }

        // 4 bytes      xxxx        Size of format section (Bytes)
    LongWord = wav_read_long (FPtr);

        // 2 bytes      xx          Format - "1" indicates PCM data
    Word = wav_read_word (FPtr);
    if (Word != 1) {
//      printf ("This function currently only handles PCM format .WAV files\n");*/
        fclose (FPtr);
        WavInfo.NumberOfSamples = 0;
        return (WavInfo);
    }
    WavInfo.DataFormat = Word;

        // 2 bytes      xx          Number of channels
    Word = wav_read_word (FPtr);
    WavInfo.NumberOfChannels = Word;

        // 4 bytes      xxxx        Sample rate (Hz)
    LongWord = wav_read_long (FPtr);
    WavInfo.SampleRate = LongWord;

        // 4 bytes      xxxx        Data rate (Bytes per second)
    LongWord = wav_read_long (FPtr);

        // 2 bytes      xx          Bytes per sample (rounded up)
    Word = wav_read_word (FPtr);
    WavInfo.BytesPerSample = Word;

        // 2 bytes      xx          Bits per sample
    Word = wav_read_word (FPtr);
    WavInfo.WordLength = Word;

        // 4 bytes      "data"      ID for data section
    *(String+4) = 0;
    do {
        *String = *(String+1);                      // Scan for "data" string
        *(String+1) = *(String+2);
        *(String+2) = *(String+3);
        *(String+3) = (char)getc (FPtr);
        if (strcmp (String, "data") == 0)
            DataSectionFound = 1;

        if (feof (FPtr)) {                          // Check end of file error
//          printf ("End of file error searching for \"data\" section\n");*/
            fclose (FPtr);
            WavInfo.NumberOfSamples = 0;
            return (WavInfo);
        }
    } while (!DataSectionFound);

        // 4 bytes      xxxx        Length of data section (Bytes)
    LongWord = wav_read_long (FPtr);
    WavInfo.NumberOfSamples = LongWord / ((int)WavInfo.BytesPerSample);

    return (WavInfo);

}       // End of wav_read_header ()



/**/
/********************************************************
* Function: wav_write_header
*
* Parameters:
*   FILE *FPtr,         - File pointer
*   const WAV_FILE_INFO WavInfo - WAV file info struct
*
* Return value:
*   int Error code
*
* Description: Write a .WAV file header section.
*
********************************************************/

void wav_write_header (FILE *FPtr,
    const WAV_FILE_INFO WavInfo)

{
    rewind (FPtr);

        // 4 bytes      "RIFF"      ID for a RIFF file
    fprintf (FPtr, "RIFF");

        // 4 bytes      xxxx        Length of waveform (Bytes)
    wav_write_long (WavInfo.BytesPerSample * WavInfo.NumberOfSamples + 36, FPtr);

        // 8 bytes      "WAVEfmt "  ID for a .WAV file
    fprintf (FPtr, "WAVEfmt ");

        // 4 bytes      xxxx        Size of format section (Bytes)
    wav_write_long (16, FPtr);

        // 2 bytes      xx          Format - "1" indicates PCM data
    wav_write_word (WavInfo.DataFormat, FPtr);

        // 2 bytes      xx          Number of channels
    wav_write_word (WavInfo.NumberOfChannels, FPtr);

        // 4 bytes      xxxx        Sample rate (Hz)
    wav_write_long (WavInfo.SampleRate, FPtr);

        // 4 bytes      xxxx        Data rate (Bytes per second)
    wav_write_long (WavInfo.BytesPerSample * WavInfo.SampleRate, FPtr);

        // 2 bytes      xx          Bytes per sample (rounded up)
    wav_write_word (WavInfo.BytesPerSample, FPtr);

        // 2 bytes      xx          Bits per sample
    wav_write_word (WavInfo.WordLength, FPtr);

        // 4 bytes      "data"      ID for data section
    fprintf (FPtr, "data");

        // 4 bytes      xxxx        Length of data section (Bytes)
    wav_write_long (WavInfo.BytesPerSample * WavInfo.NumberOfSamples, FPtr);

}       // End of wav_write_header ()



/**/
/********************************************************
* Function: wav_display_info
*
* Parameters:
*   const WAV_FILE_INFO WavInfo
*
* Return value:
*   void
*
* Description: Display an open .WAV file header info.
*
********************************************************/

void wav_display_info (const WAV_FILE_INFO WavInfo)

{
    printf (".WAV File Info.\n\n");

    printf ("Sample rate        : %d\n", WavInfo.SampleRate);
    printf ("Number of samples  : %d\n", WavInfo.NumberOfSamples);
    printf ("Number of channels : %d\n", WavInfo.NumberOfChannels);
    printf ("Word length        : %d\n", WavInfo.WordLength);
    printf ("Bytes per sample   : %d\n", WavInfo.BytesPerSample);
    printf ("Data format        : %d\n\n", WavInfo.DataFormat);

}       // End of wav_display_info ()




/**/
/********************************************************
* Function: wav_set_info
*
* Parameters:
*   const int SampleRate,           - Sample rate
*   const int NumberOfSamples,      - Number of samples
*   const short NumberOfChannels,   - Number of channels
*   const short WordLength,         - Word length
*   const short BytesPerSample,     - Bytes per sample
*   const short DataFormat          - Data format
*
* Return value:
*   WAV_FILE_INFO WavInfo           - .WAV file information.
*
* Description: Define .WAV file header info.
*
********************************************************/

WAV_FILE_INFO wav_set_info (const int SampleRate,
    const int NumberOfSamples,
    const short NumberOfChannels,
    const short WordLength,
    const short BytesPerSample,
    const short DataFormat)

{
    WAV_FILE_INFO   WavInfo;

    WavInfo.SampleRate = SampleRate;
    WavInfo.NumberOfSamples = NumberOfSamples;
    WavInfo.NumberOfChannels = NumberOfChannels;
    WavInfo.WordLength = WordLength;
    WavInfo.BytesPerSample = BytesPerSample;
    WavInfo.DataFormat = DataFormat;

    return (WavInfo);

}       // End of wav_set_info ()


/**/
/********************************************************
* Function: wav_file_length
*
* Parameters:
*   const char *fileName    - File name
*
* Return value:
*   int                     - Number of samples read, -1 for file read error
*
* Description: Return the number of samples in the .wav file
*
********************************************************/

int wav_file_length (const char *fileName)
{
    FILE *fp;
    WAV_FILE_INFO WavInfo;

    if ((fp = fopen(fileName, "rb")) == NULL) {
        return (-1);
    }

    WavInfo = wav_read_header (fp);

    fclose (fp);
    return (WavInfo.NumberOfSamples);
}

/**/
/********************************************************
* Function: wav_read_file
*
* Parameters:
*   double *pData,                   - Output array pointer
*   const char *fileName            - File name
*
* Return value:
*   WAV_FILE_INFO WavInfo           - .WAV file information.
*
* Description: Return the number of samples in the .wav file
*
********************************************************/

WAV_FILE_INFO wav_read_file (double *pData,
    const char *fileName)
{
    FILE *fp;
    WAV_FILE_INFO WavInfo;


    if ((fp = fopen(fileName, "rb")) == NULL) {
        WavInfo.NumberOfSamples = -1;
        return (WavInfo);
    }

    WavInfo = wav_read_header (fp);

    int num_samples = wav_read_data (pData, fp, WavInfo, WavInfo.NumberOfSamples);
    if (num_samples == -1) {                        // Check how many channels
        WavInfo.NumberOfSamples = -1;
    }

    fclose (fp);
    return (WavInfo);
}


/**/
/********************************************************
* Function: wav_write_file
*
* Parameters:
*   const double *pData,            - Output array pointer
*   const char *fileName,           - File name
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int                     - Number of samples written, -1 for file open error
*
* Description: Write the array to a .wav file
*
********************************************************/

int wav_write_file (const double *pData,
    const char *fileName,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)
{
    FILE *fp;

    WAV_FILE_INFO tmpWavInfo;

    tmpWavInfo.SampleRate       = WavInfo.SampleRate;
    tmpWavInfo.NumberOfSamples  = BufLen;
    tmpWavInfo.NumberOfChannels = WavInfo.NumberOfChannels;
    tmpWavInfo.WordLength       = WavInfo.WordLength;
    tmpWavInfo.BytesPerSample   = WavInfo.BytesPerSample;
    tmpWavInfo.DataFormat       = WavInfo.DataFormat;

    if ((fp = fopen(fileName, "wb")) == NULL) {
        return (-1);
    }

    wav_write_header (fp, tmpWavInfo);

    wav_write_data (pData, fp, tmpWavInfo, BufLen);

    fclose (fp);

    return (BufLen);
}


/**/
/********************************************************
* Function: wav_write_file_scaled
*
* Parameters:
*   const double *pData,            - Output array pointer
*   const char *fileName,           - File name
*   const WAV_FILE_INFO WavInfo,    - WAV file info struct
*   const int BufLen
*
* Return value:
*   int                     - Number of samples written, -1 for file open error
*
* Description: Write the array to a .wav file
*   Scales the output magnitude to 32767.0
*
********************************************************/

int wav_write_file_scaled (const double *pData,
    const char *fileName,
    const WAV_FILE_INFO WavInfo,
    const int BufLen)
{
    FILE *fp;
    double *tmppData = (double *)malloc ((unsigned long)BufLen * sizeof (double));

    if (tmppData == NULL) {
        return (-1);
    }


    WAV_FILE_INFO tmpWavInfo;

    tmpWavInfo.SampleRate       = WavInfo.SampleRate;
    tmpWavInfo.NumberOfSamples  = BufLen;
    tmpWavInfo.NumberOfChannels = WavInfo.NumberOfChannels;
    tmpWavInfo.WordLength       = WavInfo.WordLength;
    tmpWavInfo.BytesPerSample   = WavInfo.BytesPerSample;
    tmpWavInfo.DataFormat       = WavInfo.DataFormat;

    double Max = 0.0;
    for (int i = 0; i < BufLen; i++) {
        if (pData[i] >= 0.0) {
            if (pData[i] > Max) {
                Max = pData[i];
            }
        }
        else {
            if (-pData[i] > Max) {
                Max = -pData[i];
            }
        }
    }

    for (int i = 0; i < BufLen; i++) {
        tmppData[i] = pData[i] * 32767. / Max;
    }

    if ((fp = fopen(fileName, "wb")) == NULL) {
        return (-1);
    }

    wav_write_header (fp, tmpWavInfo);

    wav_write_data (tmppData, fp, tmpWavInfo, BufLen);

    free (tmppData);
    fclose (fp);

    return (BufLen);
}

#ifdef __cplusplus                  // End of decl. for C++ program calls
}
#endif

#endif
