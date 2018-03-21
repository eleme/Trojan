from ctypes import *
import fileinput
import os
import sys

#load the shared object file
adder = CDLL('./trojan_decrypt.so')

key = sys.argv[1]

inputFilePath = sys.argv[2]

parentFilePath = os.path.split(inputFilePath)[0]

outputFileName = ""

if os.path.split(inputFilePath)[1].find('.') >= 0:
	outputFileName = os.path.split(inputFilePath)[1].split('.')[0] + "-new" + '.' + os.path.split(inputFilePath)[1].split('.')[1]
else:
	outputFileName = os.path.split(inputFilePath)[1] + "-new"

outputFilePath = parentFilePath + "/" + outputFileName

if os.path.exists(outputFilePath):
	os.remove(outputFilePath)

teaDecrypt = adder.teaDecrypt
teaDecrypt.argtypes = [c_char_p, c_char_p]
teaDecrypt.restype = c_char_p

fr = open(inputFilePath, "r")
fw = open(outputFilePath, "w")
while 1:
    lines = fr.readlines(100000)
    if not lines:
        break
    for line in lines:
        pass
        if line.startswith("<Cipher>"):
        	line = line.split("<Cipher>")[1]
        	fw.write(teaDecrypt(key, line))
        	fw.write("\n")
        else:
        	fw.write(line)


fr.close()
fw.close()
