#!/bin/bash
#Run this script to stage a new version of JavaANPR to Sonatype
echo ""
echo "--------------------------------------"
echo "Running the release script of JavaANPR"
echo "--------------------------------------"
echo ""
echo "Current directory: \"`pwd`\""
echo "You should be in the root directory of JavaANPR project."
echo ""
read -p "Are you sure you're in the right directory? [Y/N]: " ans_yn
case "$ans_yn" in
	[Yy]|[Yy][Ee][Ss]) echo "Releasing from \"${PWD}\" ...";;

	*) exit 3;;
esac

echo ""
echo ""
echo "--------------------------------------"
echo "Running mvn clean -Prelease"
echo "--------------------------------------"
mvn clean -Prelease

echo ""
echo ""
echo "--------------------------------------"
echo "Running mvn release:clean -Prelease"
echo "--------------------------------------"
mvn release:clean -Prelease

echo ""
echo ""
echo "--------------------------------------"
echo "Running mvn release:prepare -Prelease"
echo "--------------------------------------"
mvn release:prepare -Prelease

echo ""
echo ""
echo "--------------------------------------"
echo "Running mvn release:perform -Prelease"
echo "--------------------------------------"
mvn release:perform -Prelease

echo ""
echo ""
echo "--------------------------------------"
echo "Running git push"
echo "--------------------------------------"
git push