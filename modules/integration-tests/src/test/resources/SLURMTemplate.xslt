<!--Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
	distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under
	the Apache License, Version 2.0 (theÏ "License"); you may not use this file except in compliance with the License. You may
	obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to
	in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
	ANY ~ KIND, either express or implied. See the License for the specific language governing permissions and limitations under
	the License. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ns="http://airavata.apache.org/gsi/ssh/2012/12">
<xsl:output method="text" />
<xsl:template match="/ns:JobDescriptor">
<xsl:choose>
<xsl:when test="ns:shellName">
#!<xsl:value-of select="ns:shellName"/>
    </xsl:when>
    </xsl:choose>
<xsl:choose>
    <xsl:when test="ns:queueName">
#SBATCH -p <xsl:value-of select="ns:queueName"/>
    </xsl:when>
    </xsl:choose>
<xsl:choose>
    <xsl:when test="ns:nodes">
#SBATCH -n <xsl:value-of select="ns:nodes"/>
    </xsl:when>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="ns:mailAddress">
#SBATCH -mail-user=<xsl:value-of select="ns:mailAddress"/>
    </xsl:when>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="ns:mailType">
#SBATCH -mail-type=<xsl:value-of select="ns:mailType"/>
    </xsl:when>
    </xsl:choose>
    <xsl:choose>
<xsl:when test="ns:acountString">
#SBATCH -A <xsl:value-of select="ns:acountString"/>
    </xsl:when>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="ns:maxWallTime">
#SBATCH -t <xsl:value-of select="ns:maxWallTime"/>
    </xsl:when>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="ns:jobName">
#SBATCH -J <xsl:value-of select="ns:jobName"/>
    </xsl:when>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="ns:standardOutFile">
#SBATCH -o <xsl:value-of select="ns:standardOutFile"/>
    </xsl:when>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="ns:standardOutFile">
#SBATCH -e <xsl:value-of select="ns:standardErrorFile"/>
    </xsl:when>
    </xsl:choose>
    <xsl:for-each select="ns:preJobCommands/ns:command">
        <xsl:value-of select="."/><xsl:text>   </xsl:text>
    </xsl:for-each>
cd <xsl:text>   </xsl:text><xsl:value-of select="ns:workingDirectory"/><xsl:text>&#xa;</xsl:text>
    <xsl:choose><xsl:when test="ns:jobSubmitterCommand">
<xsl:value-of select="ns:jobSubmitterCommand"/><xsl:text>   </xsl:text></xsl:when></xsl:choose><xsl:value-of select="ns:executablePath"/><xsl:text>   </xsl:text>
<xsl:for-each select="ns:inputs/ns:input">
      <xsl:value-of select="."/><xsl:text>   </xsl:text>
    </xsl:for-each>
</xsl:template>

</xsl:stylesheet>