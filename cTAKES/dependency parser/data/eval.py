#!/usr/bin/python
# written by Jinho D. Choi
# run from root of project
#  arg0: gold standard .dep
#  arg1: sys generated .dep
import sys
from optparse import OptionParser

parser = OptionParser()
parser.add_option("-f", "--file", dest="filename",
                  help="set of dependency labels to use: deprel.txt = FILE", metavar="FILE",
                  default = "lexiconClinQues/deprel.txt")
(options, args) = parser.parse_args()

uas   = 0
las   = 0
lcs   = 0
total = 0

fin = open(options.filename)
deprel = list()
for line in fin:
    deprel.append(line.strip())
deprel_c = []
deprel_t = []
for item in deprel:
    deprel_c.append(0)
    deprel_t.append(0)

fin1 = open(args[0])  # gold
fin2 = open(args[1])  # parse

for line1 in fin1:
    line2 = fin2.readline()
    l1 = line1.split()
    l2 = line2.split()
    if not l2       : continue
    if l2[5] in deprel: deprel_id = deprel.index(l2[5])
    else:               deprel_id = -1
    
    if l1[4] == l2[4]:
        uas += 1
        if l1[5] == l2[5]:
            las += 1
            deprel_c[deprel_id] += 1
    
    if l1[5] == l2[5]:
        lcs += 1
    
    total += 1
    if l2[5] in deprel: deprel_t[deprel_id] += 1

print 'Labeled attachment score  : %0.2f (%d/%d)' % (100.0*las/total, las, total)
print 'Unlabeled attachment score: %0.2f (%d/%d)' % (100.0*uas/total, uas, total)
print 'Label accuracy score      : %0.2f (%d/%d)' % (100.0*lcs/total, lcs, total)
print

for i, item in enumerate(deprel):
    if deprel_c[i] != 0:
        print '%s %d/%d = %0.4f' % (item, deprel_c[i], deprel_t[i], 1.0*deprel_c[i]/deprel_t[i])
