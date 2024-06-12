import {
  FormControl,
  FormLabel,
  FormErrorMessage,
  Flex,
  RadioGroup,
  Radio,
  FormHelperText,
  Box, Container, Img, Input, Text, Select, Heading, Link, HStack, VStack, Stack,
  Button,
  Textarea,
  Checkbox,
  Spacer
} from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { useEffect, useState } from "react";
import { Footer } from "../components/Footer";

const Home = () => {
  const [userName, setUserName] = useState('');
  const [email, setEmail] = useState('');

  useEffect(() => {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const obj = JSON.parse(atob(accessToken.split('.')[1]));

      setUserName(obj.name);
      setEmail(obj.email);
    } catch (error) {
      console.log(error);

      window.location.href = "/login";
    }
  }, []);

  const [name, setName] = useState("NAMD on " + new Date().toLocaleDateString() + " at " + new Date().toLocaleTimeString());

  const [desc, setDesc] = useState("Enter description here");
  const [descOpen, setDescOpen] = useState(false);

  const [project, setProject] = useState("option1");
  const [executionType, setExecutionType] = useState("CPU");

  const [contPrev, setContPrev] = useState(true);

  const [replicate, setReplicate] = useState(false);

  const [allocation, setAllocation] = useState("default");
  const [computeResource, setComputeResource] = useState("expanse");

  const [nodeCount, setNodeCount] = useState(1);
  const [coreCount, setCoreCount] = useState(128);
  const [timeLimit, setTimeLimit] = useState(2);
  const [physMemory, setPhysMemory] = useState(1);

  const [emailNotif, setEmailNotif] = useState(false);

  const [settingsOpen, setSettingsOpen] = useState(false);
  const [queue, setQueue] = useState("compute");



  useEffect(() => {
    console.log("project: ", project);
  }, [project]);
  return (
    <>
      <HeaderBox name={userName} email={email} />

      <Container maxW='container.md' p={4} mt={4}>
        <Stack direction='column' spacing={4}>

          <Text>NAMD</Text>

          <Heading mt={-4} fontSize='3xl'>Create a New Experiment</Heading>


          <FormControl>
            <FormLabel>Experiment Name</FormLabel>
            <Input type='text' value={name} onChange={(e) => setName(e.target.value)} />
          </FormControl>

          <Text _hover={{
            'textDecoration': "underline",
            'cursor': "pointer"
          }} onClick={
            () => {
              setDescOpen(!descOpen);
            }
          }>{
              descOpen ? "Hide Description" : "Add Description"
            }
          </Text>

          {
            descOpen && (

              <FormControl>
                <FormLabel>Description</FormLabel>
                <Textarea type='text' value={desc} onChange={(e) => setDesc(e.target.value)} />
              </FormControl>
            )
          }


          {/* Project input */}
          <FormControl>
            <FormLabel>Project</FormLabel>
            <Select placeholder='Select option' value={project} onChange={(e) => {
              setProject(e.target.value);
            }}>
              <option value='option1'>Default Project</option>
              <option value='option2'>Option 2</option>
              <option value='option3'>Option 3</option>
            </Select>
          </FormControl>

          <Box bg='blue.100' p={2} rounded='md' fontSize='sm'>
            <Text>Some needed input file can be uploaded as optional now! The restart and coordinate files are being downloaded.
            </Text>
          </Box>

          <Heading mt={4} fontSize='3xl'>Application Configuration</Heading>

          <Heading fontSize='xl'>Application Inputs</Heading>

          <FormControl>
            <RadioGroup onChange={setExecutionType} value={executionType}>
              <Stack direction='column'>
                <Radio value="CPU">CPU</Radio>

                <Radio value="GPU">GPU</Radio>
              </Stack>
            </RadioGroup>

            <FormHelperText>CPU or GPU executable to be used. If you chose GPU please make sure GPU partitions are selected at the Resource selection below.
            </FormHelperText>
          </FormControl>



          <FormControl>
            <FormLabel>Continue from Previous Run</FormLabel>
            <Checkbox isChecked={contPrev} onChange={(e) => {
              setContPrev(e.target.checked);
            }}>Yes</Checkbox>
          </FormControl>


          <FormControl>
            <FormLabel>MD-Instructions-Input</FormLabel>
            <Input type='file' placeholder='upload file' />
            <FormHelperText>NAMD conf file/QuickMD conf file.</FormHelperText>
          </FormControl>

          <FormControl>
            <FormLabel>Protein-Structure-File_PSF</FormLabel>
            <Input type='file' placeholder='upload file' />
            <FormHelperText>Protein structure file (psf) needed but could be uploaded using optional upload below together with other needed files
            </FormHelperText>
          </FormControl>


          <FormControl>
            <FormLabel>FF-Parameter-Files</FormLabel>
            <Input type='file' placeholder='upload file' />
            <FormHelperText>Force field parameter and related files (e.g, *.prm and *.str files) needed but could be uploaded using optional upload below together with other needed files
            </FormHelperText>
          </FormControl>

          <FormControl>
            <FormLabel>Constraints-PDB</FormLabel>
            <Input type='file' placeholder='upload file' />
            <FormHelperText>Constraints file in pdb
            </FormHelperText>
          </FormControl>

          <FormControl>
            <FormLabel>Optional_Inputs
            </FormLabel>
            <Input type='file' placeholder='upload file' />
            <FormHelperText>Any other optional and all needed inputs to be uploaded, for a modified DCD out please upload your instructions for modification in a file named ModDCD.tcl.
            </FormHelperText>
          </FormControl>


          <FormControl>
            <FormLabel>Replicate</FormLabel>
            <Checkbox isChecked={replicate} onChange={(e) => {
              setReplicate(e.target.checked);
            }}>Yes</Checkbox>
            <FormHelperText>Optionally Specify if Replicated runs needed. Make sure the resources requested are commensurate, such as as many nodes as replicas.</FormHelperText>
          </FormControl>


          <FormControl>
            <FormLabel>Allocation</FormLabel>

            <Select placeholder='Select an allocation' value={allocation} onChange={(e) => {
              setAllocation(e.target.value);
            }}>
              <option value='default'>Default</option>
              <option value='personal'>Diego's Personal</option>
              <option value='option3'>Fatemeh's Profile</option>
            </Select>
          </FormControl>

          <FormControl>
            <FormLabel>Compute Resource</FormLabel>

            <Select placeholder='Select a compute resource' value={computeResource} onChange={(e) => {
              setComputeResource(e.target.value);
            }}>
              <option value='expanse'>Expanse</option>
              <option value='bridges2'>Bridges2</option>
              <option value='ncsasdelta'>NCSADelta</option>
            </Select>
          </FormControl>

          <Box p={4} rounded='md' border='1px solid gray' _hover={{
            bg: 'gray.100',
            cursor: 'pointer'
          }} onClick={
            () => {
              setSettingsOpen(!settingsOpen);
            }
          }>
            <Heading fontSize='2xl'>Settings for queue complete</Heading>
            <Flex justify='space-between' mt={4}>
              <Box>
                <Heading>{nodeCount}</Heading>
                <Text>Node Count</Text>
              </Box>

              <Box>
                <Heading>{coreCount}</Heading>
                <Text>Core Count</Text>
              </Box>

              <Box>
                <Heading>{timeLimit} minutes</Heading>
                <Text>Time Limit</Text>
              </Box>

              <Box>
                <Heading>{physMemory} MB</Heading>
                <Text>Physical Memory</Text>
              </Box>

            </Flex>
          </Box>

          {
            settingsOpen && (
              <Stack direction='column' spacing={4}>

                <FormControl>
                  <FormLabel>Select a Queue</FormLabel>
                  <Select placeholder='Select a queue' value={queue} onChange={(e) => {
                    setQueue(e.target.value);
                  }}>
                    <option value='compute'>compute</option>
                    <option value='gpu'>gpu</option>
                    <option value='gpu-shared'>gpu-shared</option>
                    <option value='shared'>shared</option>
                  </Select>
                </FormControl>
                <FormControl>
                  <FormLabel>Node Count</FormLabel>
                  <Input type='number' value={nodeCount} onChange={(e) => setNodeCount(e.target.value)} />
                  <FormHelperText> Max Allowed Nodes = 728
                  </FormHelperText>
                </FormControl>

                <FormControl>
                  <FormLabel>Total Core Count</FormLabel>
                  <Input type='number' value={coreCount} onChange={(e) => setNodeCount(e.target.value)} />
                  <FormHelperText> Max Allowed Cores = 93184. There are 128 cores per node.
                  </FormHelperText>
                </FormControl>


                <FormControl>
                  <FormLabel>Wall Time Limit</FormLabel>
                  <Input type='number' value={timeLimit} onChange={(e) => setTimeLimit(e.target.value)} />
                  <FormHelperText>Max Allowed Wall Time = 2880 minutes
                  </FormHelperText>
                </FormControl>


                <FormControl>
                  <FormLabel>Physical Memory</FormLabel>
                  <Input type='number' value={physMemory} onChange={(e) => setPhysMemory(e.target.value)} />
                  <FormHelperText>Max Allowed Physical Memory = 186368000 MB
                  </FormHelperText>
                </FormControl>
              </Stack>

            )
          }

          <FormControl>
            <FormLabel>Email Settings</FormLabel>
            <Checkbox isChecked={emailNotif} onChange={(e) => {
              setEmailNotif(e.target.checked);
            }}>Receive email notification of experiment status</Checkbox>
          </FormControl>

          <Flex>
            <Box></Box>

            <Spacer />
            <HStack>
              <Button colorScheme='green'>Save and Launch</Button>
              <Button colorScheme='blue'>Save</Button>
            </HStack>
          </Flex>

        </Stack>
      </Container>

      <Footer />
    </>
  );
};

export default Home;;