#include <jni.h>
#include <string>
#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include <sstream>
#include <vector>
#include <iterator>
using namespace std;

std::string processar(const char *pathIn, const char *pathOut) {
    std::string retorno = std::string(pathIn) + " - ";
    streampos size;
    char* memblock = new char[4096];
    int numBytesRead;

    ifstream file (pathIn, ios::in|ios::binary|ios::ate);
    // the file is open with the ios::ate flag, which means that the get pointer will be positioned at the end of the file
    if (file.is_open())
    {
        // char[] magica = {0x4D, 0x6F, 0x74, 0x69, 0x6F, 0x6E, 0x50, 0x68, 0x6F, 0x74, 0x6F, 0x5F, 0x44, 0x61, 0x74, 0x61}
        string magica = "MotionPhoto_Data";
        int iSuc = 0;
        long iFile = 0;
        size = file.tellg();
        memblock = new char [size];
        ofstream *outfile = nullptr;
        file.seekg (0, ios::beg);
        while(iFile < size) {
            file.read (memblock, 4096);
            if (file.failbit) {
                // std::cout << "ERRO, failbit " << iFile << "/" << size << std::endl;
                // break;
            }
            numBytesRead = (long)size - (long)iFile;
            if (numBytesRead > 4096) numBytesRead = 4096;
            if (numBytesRead == 0) {
                retorno.append("ERRO, numBytesRead inválido");
                break;
            }
            else if (numBytesRead > 0) {
                if (outfile != nullptr) {
                    outfile->write (memblock, numBytesRead);
                    iFile += numBytesRead;
                    continue;
                }
                for (int iBuf = 0; iBuf < numBytesRead; iBuf++, iFile++) {
                    if (memblock[iBuf] == magica[iSuc]) {
                        iSuc ++;
                        if (iSuc == magica.length()) {
                            retorno.append("Encontrado MotionPhoto_Data");
                            outfile = new ofstream();
                            outfile->open (pathOut);
                            iBuf++;
                            iFile++;
                            outfile->write (memblock + iBuf, numBytesRead - iBuf);
                            iFile += numBytesRead - iBuf;
                            iBuf += numBytesRead - iBuf;
                            break;
                        }
                        else continue;
                    }
                    else iSuc = 0;
                }
                // if (iSuc == magica.length()) break;
            }
            else {
                retorno.append("ERRO, numBytesRead inválido");
                break;
            }
        }
        file.close();
        if (outfile != nullptr)
            outfile->close();
        if (iFile == size && iSuc != magica.length()) {
            retorno.append("Arquivo não é motion.");
        }
        delete[] memblock;
    }
    else retorno.append("Unable to open file");

    return retorno;
}

extern "C" JNIEXPORT jstring

JNICALL
Java_com_calangoni_platcplmob_1t02_feature_MainActivity_stringFromJNI(
        JNIEnv *pEnv,
        jobject /* this */,
        jstring pOrigem,
        jstring pDestino) {

    const char* tmpOrigem = pEnv->GetStringUTFChars(pOrigem, NULL);
    const char* tmpDestino = pEnv->GetStringUTFChars(pDestino, NULL);

    std::string retorno = "Hello from C++: " + std::string(tmpOrigem) + " <-> " + std::string(tmpDestino);

    pEnv->ReleaseStringUTFChars(pOrigem, tmpOrigem);
    pEnv->ReleaseStringUTFChars(pDestino, tmpDestino);

    return pEnv->NewStringUTF(retorno.c_str());
}

extern "C" JNIEXPORT jstring

JNICALL
Java_com_calangoni_platcplmob_1t02_feature_MainActivityFragment_procurarSepararVideo(
        JNIEnv *pEnv,
        jobject /* this */,
        jstring pOrigem,
        jstring pDestino) {

    const char* tmpOrigem = pEnv->GetStringUTFChars(pOrigem, NULL);
    const char* tmpDestino = pEnv->GetStringUTFChars(pDestino, NULL);

    std::string retorno = processar(tmpOrigem, tmpDestino);

    pEnv->ReleaseStringUTFChars(pOrigem, tmpOrigem);
    pEnv->ReleaseStringUTFChars(pDestino, tmpDestino);

    return pEnv->NewStringUTF(retorno.c_str());
}
