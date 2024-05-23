export const titleCase = (str) =>
{
    return str.toLowerCase().replace(/\b\w/g, s => s.toUpperCase());
};
