// layout.tsx
import NavBar from "@/layouts/NavBar";
import { Outlet } from "react-router";

const NavBarFooterLayout = () => {
  return (
    <>
      <NavBar />
      <Outlet />
    </>
  );
};

export default NavBarFooterLayout;
