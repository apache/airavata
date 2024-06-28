import dynamic from 'next/dynamic';

const VNCItem = dynamic(() => {
  return import('../components/VNCItem').then((mod) => mod.VNCItem);
}, { ssr: false });


export const VNCTest = () => {
  return (
    <VNCItem
      url="ws://20.51.202.251:9007"
      username="user"
      password="1234"
      handleOnDisconnect={() => { console.log("Disconnected"); }}
    />
  );
};