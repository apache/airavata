/*****************************************************************
*
*  Licensed to the Apache Software Foundation (ASF) under one  
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information       
*  regarding copyright ownership.  The ASF licenses this file  
*  to you under the Apache License, Version 2.0 (the           
*  "License"); you may not use this file except in compliance  
*  with the License.  You may obtain a copy of the License at  
*                                                              
*    http://www.apache.org/licenses/LICENSE-2.0                
*                                                              
*  Unless required by applicable law or agreed to in writing,  
*  software distributed under the License is distributed on an 
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY      
*  KIND, either express or implied.  See the License for the   
*  specific language governing permissions and limitations     
*  under the License.                                          
*                                                              
*
*****************************************************************/
class Logger {
    constructor() {
        this.levels = {
            INFO: 'INFO',
            WARN: 'WARN',
            ERROR: 'ERROR'
        };

        this.colors = {
            INFO: 'color: lightblue',
            WARN: 'color: yellow',
            ERROR: 'color: red'
        };
    }

    getTimestamp() {
        return new Date().toISOString();
    }

    log(level, message) {
        if (!this.levels[level]) {
            throw new Error(`Unknown level: ${level}`);
        }

        const timestamp = this.getTimestamp();
        const color = this.colors[level];

        console.log(`%c[${timestamp}] [${level}]%c ${message}`,
            color, '',
        );
    }

    info(message) {
        this.log(this.levels.INFO, message);
    }

    warn(message) {
        this.log(this.levels.WARN, message);
    }

    error(message) {
        this.log(this.levels.ERROR, message);
    }
}


// Example usage:
export const logger = new Logger();

