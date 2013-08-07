#!/usr/bin/perl

use strict;
my $num_parens=0;
my $tree = "";

while(<STDIN>){
  chomp;
  ## Get rid of leading spaces
  s/^\s*//;

  if($_ eq ""){
    next;
  }

  # Accumulate tree lines until parens match
  $tree .= "$_ ";
  my @left_parens = m/(\()/g;
  my @right_parens = m/(\))/g;
  $num_parens += ($#left_parens + 1);
  $num_parens -= ($#right_parens + 1);
  if($num_parens == 0){
    ## Get rid of extra parentheses around the tree
      my @tokens = ( $tree =~ m/(?<=\()[^() ]+ [^() ]+(?=\))/g );
      foreach my $token (@tokens) {
	  $token =~ s/ /_/g;
	  print $token." ";
      }
      print "\n";
#    $tree =~ s/^\s*\(\s*\((.*)\s*\)\s*\)\s*$/(\1)/;
#    print "$tree\n";
#      print join(" ",@tokens);
    $tree = "";
  }

}
