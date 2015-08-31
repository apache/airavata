<!--Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
	distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under
	the Apache License, Version 2.0 (theÏ "License"); you may not use this file except in compliance with the License. You may
	obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to
	in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
	ANY ~ KIND, either express or implied. See the License for the specific language governing permissions and limitations under
	the License. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ns="http://airavata.apache.org/gfac/core/2012/12">
<xsl:output method="text" />
<xsl:template match="/ns:JobDescriptor">
#! /bin/sh
cd <xsl:text>   </xsl:text><xsl:value-of select="ns:workingDirectory"/><xsl:text>&#xa;</xsl:text>
    <xsl:choose><xsl:when test="ns:jobSubmitterCommand">
<xsl:value-of select="ns:jobSubmitterCommand"/><xsl:text>   </xsl:text></xsl:when></xsl:choose><xsl:value-of select="ns:executablePath"/><xsl:text>   </xsl:text>
<xsl:for-each select="ns:inputs/ns:input">
      <xsl:value-of select="."/><xsl:text>   </xsl:text>
    </xsl:for-each>
<xsl:for-each select="ns:postJobCommands/ns:command">
      <xsl:value-of select="."/><xsl:text>   </xsl:text>
</xsl:for-each>

</xsl:template>

</xsl:stylesheet>