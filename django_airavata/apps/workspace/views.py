
import json
import logging
from urllib.parse import urlparse

from airavata.model.application.io.ttypes import DataType
from airavata_django_portal_sdk import user_storage as user_storage_sdk
from django.contrib.auth.decorators import login_required
from django.shortcuts import render
from rest_framework.renderers import JSONRenderer

from django_airavata.apps.api.views import (
    ApplicationModuleViewSet,
    ExperimentSearchViewSet,
    FullExperimentViewSet,
    ProjectViewSet
)

logger = logging.getLogger(__name__)


@login_required
def experiments_list(request):
    request.active_nav_item = 'experiments'

    response = ExperimentSearchViewSet.as_view({'get': 'list'})(request)
    if response.status_code != 200:
        raise Exception("Failed to load experiments list: {}".format(
            response.data['detail']))
    experiments_json = JSONRenderer().render(response.data).decode('utf-8')
    return render(request, 'django_airavata_workspace/experiments_list.html', {
        'bundle_name': 'experiment-list',
        'experiments_data': experiments_json
    })


@login_required
def dashboard(request):
    request.active_nav_item = 'dashboard'
    return render(request, 'django_airavata_workspace/dashboard.html', {
        'bundle_name': 'dashboard',
        'sidebar': True,
    })


@login_required
def projects_list(request):
    request.active_nav_item = 'projects'

    response = ProjectViewSet.as_view({'get': 'list'})(request)
    if response.status_code != 200:
        raise Exception("Failed to load projects list: {}".format(
            response.data['detail']))
    projects_json = JSONRenderer().render(response.data).decode('utf-8')

    return render(request, 'django_airavata_workspace/projects_list.html', {
        'bundle_name': 'project-list',
        'projects_data': projects_json
    })


@login_required
def edit_project(request, project_id):
    request.active_nav_item = 'projects'

    return render(request, 'django_airavata_workspace/edit_project.html', {
        'bundle_name': 'edit-project',
        'project_id': project_id
    })


def species_list(request):
    return {
        'species': ["ALMANDINE","ANDRADITE","GROSSULAR","KNORRINGITE","MAJORITE","PYROPE","SPESSARTINE","CLINOHUMITE","FAYALITE","FORSTERITE","MONTICELLITE","TEPHROITE","ANDALUSITE","KYANITE","Al-MULLITE","Si-MULLITE","Fe-CHLORITOID","Mg-CHLORITOID","Mn-CHLORITOID","Fe-STAUROLITE","Mg-STAUROLITE","Mn-STAUROLITE","HYDROXY-TOPAZ","AKERMANITE","JULGOLDITE(FeFe)","MERWINITE","PUMPELLYITE(FeAl)","PUMPELLYITE(MgAl)","RANKINITE","SPURRITE","TILLEYITE","ZIRCON","CLINOZOISITE","EPIDOTE(ORDERED)","Fe-EPIDOTE","LAWSONITE","PIEMONTITE(ORDERED)","ZOISITE","VESUVIANITE","Fe-OSUMILITE","OSUMILITE(1)","OSUMILITE(2)","Fe-AKIMOTOITE","AKIMOTOITE","CaSi-TITANITE","Al-PEROVSKITE","Ca-PEROVSKITE","Fe-PEROVSKITE","Mg-PEROVSKITE","PHASEA","Fe-RINGWOODITE","Mg-RINGWOODITE","Fe-WADSLEYITE","Mg-WADSLEYITE","ACMITE","Ca-ESKOLA_PYROXENE","CLINO_ENSTATITE","HI-P_CLINOENSTATITE","DIOPSIDE","ENSTATITE","FERROSILITE","HEDENBERGITE","JADEITE","KOSMOCHLOR","Mg-TSCHERMAKS_PX","PROTOENSTATITE","PSEUDOWOLLASTONITE","PYROXMANGITE","RHODONITE","WALSTROMITE","WOLLASTONITE","ANTHOPHYLLITE","Fe-ANTHOPHYLLITE","CUMMINGTONITE","FERROACTINOLITE","FERROGLAUCOPHANE","GLAUCOPHANE","GRUNERITE","PARGASITE","RIEBECKITE","TREMOLITE","TSCHERMAKITE","DEERITE","FERROCARPHOLITE","MAGNESIOCARPHOLITE","Fe-SAPPHIRINE(221)","SAPPHIRINE(221)","SAPPHIRINE(351)","ANNITE","CELADONITE","FERROCELADONITE","EASTONITE","MARGARITE","Mn-BIOTITE","MUSCOVITE","SODAPHLOGOPITE","PARAGONITE","PHLOGOPITE","Al-FREE_CHLORITE","AMESITE(14A)","CLINOCHLORE(ORDERED)","DAPHNITE","Mn-CHLORITE","FERROSUDOITE","SUDOITE","ANTIGORITE","CHRYSOTILE","FERROTALC","GREENALITE","KAOLINITE","LIZARDITE","MINNESOTAITE","Mg-MINNESOTAITE","PREHNITE","PRL-TALC","PYROPHYLLITE","FERROSTILPNOMELANE","Mg-STILPNOMELANE","TALC","TSCHERMAK-TALC","FERRI-PREHNITE","ALBITE(HIGH)","ANALCITE","CARNEGIEITE(HIGH)","CARNEGIEITE(LOW)","K-CYMRITE","KALSILITE","MICROCLINE","COESITE","CRISTOBALITE(HIGH)","STISHOVITE","TRIDYMITE(HIGH)","HEULANDITE","HOLLANDITE","LAUMONTITE","MEIONITE","SODALITE","STILBITE","Si-WADEITE","WAIRAKITE","BADDELEYITE","BIXBYITE","CORUNDUM","CUPRITE","ESKOLAITE","GEIKIELITE","LIME","MANGANOSITE","MgSi-CORUNDUM","PERICLASE","FERROPERICLASE","PYROPHANITE","RUTILE","TENORITE","ULVOSPINEL","BRUCITE","DIASPORE","GOETHITE","MAGNESITE","RHODOCHROSITE","SIDERITE","ANHYDRITE","HALITE","PYRITE","SYLVITE","COPPER","DIAMOND","GRAPHITE","SULPHUR","GIBBSITE","BOEHMITE","FLUORPHLOGOPITE","HYDROXYAPATITE","FLUORAPATITE","CHLORAPATITE","ARSENIC","ARSENOLITE","CLAUDETITE","As2O5,s","REALGAR(ALPHA)","REALGAR(BETA)","ORPIMENT","ORPIMENT(AM)","ARSENOPYRITE","SCORODITE","FERRIC-As(AM)","BARIUM-As","BARIUM-H-As","a-GaOOH","DAWSONITE","SAPPHIRINE(793)","Fe-SAPPHIRINE(793)","GEDRITE","DOLOMITE-DIS","DOLOMITE-ORD","GALENA","BARITE","FLUORITE","CELESTITE","ANGLESITE","CHALCEDONY","SiO2(a)","LARNITE","SPHENE","QUARTZ","NEPHELINE","HEMATITE","NICKEL_OXIDE","ILMENITE","MAGNETITE","MAGNESIOFERRITE","CALCITE","ARAGONITE","PYRRHOTITE(TROT)","TROILITE","LOW_TROILITE","PYRRHOTITE(TROV)","IRON","NICKEL","SILLIMANITE","GEHLENITE","CORDIERITE","HYDROUS-CORDIERITE","Fe-CORDIERITE","Mn-CORDIERITE","Ca-TSCHERMAKS_PX","ALBITE","SANIDINE","ANORTHITE","LEUCITE","SPINEL","HERCYNITE","PICROCHROMITE","DOLOMITE","ANKERITE","CH4,g","CO,g","CO2,g","H2,g","H2S,g","O2,g","S2,g","H2O,g","Ar,g","PHENOL,g","ORTHO-CRESOL,g","META-CRESOL,g","PARA-CRESOL,g","ETHYLENE,g","He,g","Kr,g","N2,g","Ne,g","NH3,g","Rn,g","SO2,g","Xe,g","NO,g","N2O,g","Ag(CO3)-","Ag(CO3)2-3","Ag+","Ag+2","AgCl,aq","AgCl2-","AgCl3-2","AgCl4-3","AgOH,aq","AgO-","AgNO3,aq","AlOH+2","Al+3","AlH3SiO4+2","Al(OH)3,aq","Al(OH)4-","Al(OH)2+","Au+","Au+3","B(OH)3,aq","BF4-","BO2-","Ba(HCO3)+","Ba+2","BaCl+","BaOH+","Be+2","BeOH+","BeO,aq","HBeO2-","BeO2-2","Br-","Br3-","HBrO,aq","BrO-","BrO3-","BrO4-","CaOH+","Ce+4","CO,aq","CO2,aq","CO3-2","Ca(HCO3)+","Ca+2","CaCl+","CaCl2,aq","CaF+","CaSO4,aq","Cd+2","CdOH+","CdO,aq","HCdO2-","CdO2-2","Ce+2","Ce+3","Cl-","HClO,aq","ClO-","ClO2-","ClO3-","ClO4-","Co+2","Co+3","CoOH+","CoO,aq","HCoO2-","CoO2-2","CoOH+2","Cr2O7-2","CrO4-2","Cs+","CsBr,aq","CsCl,aq","CsI,aq","CsOH,aq","Cu+","Cu+2","CuOH+","CuO,aq","HCuO2-","CuO2-2","Dy+2","Dy+3","Dy+4","Er+2","Er+3","Er+4","Eu+2","Eu+3","Eu+4","F-","Fe+2","Fe+3","FeCl+","FeCl2,aq","FeOH+2","FeOH+","FeO+","FeO,aq","HFeO2-","HFeO2,aq","FeO2-","Ga+3","GaOH+2","GaO+","HGaO2,aq","GaO2-","Gd+2","Gd+3","Gd+4","H+","H2,aq","NaH2AsO4,aq","KH2AsO4,aq","MgH2AsO4+","CaH2AsO4+","SrH2AsO4+","MnH2AsO4+","FeH2AsO4+","CoH2AsO4+","NiH2AsO4+","CuH2AsO4+","ZnH2AsO4+","PbH2AsO4+","AlH2AsO4+2","FeH2AsO4+2","NaHAsO4-","KHAsO4-","MgHAsO4,aq","CaHAsO4,aq","SrHAsO4,aq","MnHAsO4,aq","FeHAsO4,aq","CoHAsO4,aq","NiHAsO4,aq","CuHAsO4,aq","ZnHAsO4,aq","PbHAsO4,aq","AlHAsO4+","FeHAsO4+","NaAsO4-2","KAsO4-2","MgAsO4-","CaAsO4-","SrAsO4-","MnAsO4-","FeAsO4-","CoAsO4-","NiAsO4-","CuAsO4-","ZnAsO4-","PbAsO4-","AlAsO4,aq","FeAsO4,aq","NaH2AsO3,aq","AgH2AsO3,aq","MgH2AsO3+","CaH2AsO3+","SrH2AsO3+","BaH2AsO3+","CuH2AsO3+","PbH2AsO3+","AlH2AsO3+2","FeH2AsO3+2","H2P2O7-2","H2PO4-","H2S,aq","H3VO4,aq","H2VO4-","H3P2O7-","H3PO4,aq","HCO3-","HCrO4-","HF,aq","HF2-","HNO3,aq","HO2-","HPO4-2","HS-","HSO3-","HSO4-","HSO5-","HSe-","H2SeO3,aq","HSeO3-","HSeO4-","HSiO3-","HSiO3-M","HVO4-2","He,aq","Hg+2","HgOH+","HgO,aq","HHgO2-","Hg2+2","Ho+2","Ho+3","Ho+4","I-","I3-","HIO,aq","IO-","IO3-","IO4-","In+3","InOH+2","InO+","HInO2,aq","InO2-","K+","KBr,aq","KCl,aq","KAlO2,aq","KHSO4,aq","KI,aq","KOH,aq","KSO4-","Kr,aq","La+3","Li+","LiCl,aq","LiOH,aq","Lu+3","Lu+4","Mg(HCO3)+","Mg+2","MgCl+","MgF+","MgOH+","Mn+2","Mn+3","MnCl+","MnOH+","MnO,aq","HMnO2-","MnO2-2","MnO4-","MnO4-2","MnSO4,aq","HMoO4-","MoO4-2","N2,aq","NH3,aq","NH4+","NO2-","NO3-","Na+","NaAl(OH)4,aq","NaBr,aq","NaCl,aq","NaF,aq","NaHSiO3,aq","NaI,aq","NaOH,aq","Nd+2","Nd+3","Nd+4","Ne,aq","Ni+2","NiCl+","NiOH+","NiO,aq","HNiO2-","NiO2-2","O2,aq","OH-","PO4-3","Pb+2","PbCl+","PbCl2,aq","PbCl3-","PbCl4-2","PbOH+","PbO,aq","HPbO2-","Pd(OH)+","PdSO4,aq","Pd(SO4)2-2","Pd(SO4)3-4","Pd+2","PdCl+","PdCl2,aq","PdCl3-","PdCl4-2","PdO,aq","Pr+2","Pr+3","Pr+4","Pt(OH)+","PtSO4,aq","Pt(SO4)2-2","Pt(SO4)3-4","Pt+2","PtCl+","PtCl2,aq","PtCl3-","PtCl4-2","PtO,aq","Ra+2","Rb+","RbBr,aq","RbCl,aq","RbF,aq","RbI,aq","RbOH,aq","ReO4-","Rh(OH)+","Rh(OH)+2","RhSO4,aq","Rh(SO4)+","Rh(SO4)2-","Rh(SO4)2-2","Rh(SO4)3-3","Rh(SO4)3-4","Rh+2","Rh+3","RhCl+","RhCl+2","RhCl2,aq","RhCl2+","RhCl3,aq","RhCl3-","RhCl4-","RhCl4-2","RhO,aq","RhO+","Rn,aq","Ru(OH)+","Ru(OH)+2","RuO4-2","RuSO4,aq","Ru(SO4)+","Ru(SO4)2-","Ru(SO4)2-2","Ru(SO4)3-3","Ru(SO4)3-4","Ru+2","Ru+3","RuCl+","RuCl+2","RuCl2,aq","RuCl2+","RuCl3,aq","RuCl3-","RuCl4-","RuCl4-2","RuCl5-2","RuCl6-3","RuO,aq","RuO+","S2-2","S2O3-2","HS2O3-","H2S2O3,aq","S2O4-2","HS2O4-","H2S2O4,aq","S2O5-2","S2O6-2","S2O8-2","S3-2","S3O6-2","S4-2","S4O6-2","S5-2","S5O6-2","SO2,aq","SO3-2","SO4-2","Sc+3","ScOH+2","ScO+","HScO2,aq","ScO2-","SeO3-2","SeO4-2","SiF6-2","SiO2,aq","SiO2M,aq","Sm+2","Sm+3","Sm+4","Sn+2","SnO,aq","SnOH+","HSnO2-","Sr(HCO3)+","Sr+2","SrCl+","SrF+","SrOH+","Tb+2","Tb+3","Tb+4","Th+4","Tl+","Tl+3","TlOH,aq","TlOH+2","TlO+","HTlO2,aq","TlO2-","Tm+2","Tm+3","Tm+4","U+3","U+4","UO2+","UO2+2","VO+","VO+2","VOH+","VOH+2","VO2+","VOOH+","WO4-2","HWO4-","Xe,aq","Y+3","YOH+2","YO+","HYO2,aq","YO2-","Yb+2","Yb+3","Yb+4","Zn+2","ZnCl+","ZnCl2,aq","ZnCl3-","ZnOH+","ZnO,aq","HZnO2-","ZnO2-2","U(OH)+2","UO+","HUO2,aq","U(OH)+3","UO+2","HUO2+","UO2,aq","HUO3-","UO2OH,aq","UO3-","UO2OH+","UO3,aq","HUO4-","UO4-2","Fr+","HNO2,aq","H2N2O2,aq","HN2O2-","N2O2-2","N2H5+","N2H6+2","H3PO2,aq","H2PO2-","H3PO3,aq","H2PO3-","HPO3-2","P2O7-4","HP2O7-3","H4P2O7,aq","AsO4-3","H3AsO4,aq","H2AsO4-","HAsO4-2","HAsO2,aq","H2AsO3-","HAsO3-2","AsO3-3","As3S4(HS)2-","HSbO2,aq","SbO2-","Bi+3","BiO+","BiOH+2","HBiO2,aq","BiO2-","H2O2,aq","HClO2,aq","HIO3,aq","V+2","V+3","VO4-3","Cr+2","Cr+3","CrOH+2","CrO+","HCrO2,aq","CrO2-","Zr+4","Zr(OH)+3","ZrO+2","HZrO2+","ZrO2,aq","HZrO3-","HNbO3,aq","NbO3-","TcO4-","Pm+2","Pm+3","Pm+4","Hf+4","HfOH+3","HfO+2","HHfO2+","HfO2,aq","HHfO3-","La+2","LaCO3+","LaHCO3+2","LaOH+2","LaO+","LaO2H,aq","LaO2-","LaCl+2","LaCl2+","LaCl3,aq","LaCl4-","LaNO3+2","LaF+2","LaF2+","LaF3,aq","LaF4-","LaH2PO4+2","LaSO4+","CeCO3+","CeHCO3+2","CeOH+2","CeO+","CeO2H,aq","CeO2-","CeCl+2","CeCl2+","CeCl3,aq","CeCl4-","CeH2PO4+2","CeNO3+2","CeF+2","CeF2+","CeF3,aq","CeF4-","CeBr+2","CeIO3+2","CeClO4+2","CeSO4+","PrCO3+","PrHCO3+2","PrCl+2","PrCl2+","PrCl3,aq","PrCl4-","PrH2PO4+2","PrNO3+2","PrF+2","PrF2+","PrF3,aq","PrF4-","PrOH+2","PrO+","PrO2H,aq","PrO2-","PrSO4+","NdCO3+","NdHCO3+2","NdOH+2","NdO+","NdO2H,aq","NdO2-","NdCl+2","NdCl2+","NdCl3,aq","NdCl4-","NdH2PO4+2","NdNO3+2","NdF+2","NdF2+","NdF3,aq","NdF4-","NdSO4+","SmCO3+","SmOH+2","SmO+","SmO2H,aq","SmO2-","SmHCO3+2","SmCl+2","SmCl2+","SmCl3,aq","SmCl4-","SmH2PO4+2","SmNO3+2","SmF+2","SmF2+","SmF3,aq","SmF4-","SmSO4+","EuCO3+","EuOH+2","EuO+","EuO2H,aq","EuO2-","EuHCO3+2","EuCl+2","EuCl2+","EuCl3,aq","EuCl4-","EuF+","EuF2,aq","EuF3-","EuF4-2","EuCl+","EuCl2,aq","EuCl3-","EuCl4-2","EuH2PO4+2","EuNO3+2","EuF+2","EuF2+","EuF3,aq","EuF4-","EuSO4+","GdCO3+","GdOH+2","GdO+","GdO2H,aq","GdO2-","GdHCO3+2","GdCl+2","GdCl2+","GdCl3,aq","GdCl4-","GdH2PO4+2","GdNO3+2","GdF+2","GdF2+","GdF3,aq","GdF4-","GdSO4+","TbCO3+","TbOH+2","TbO+","TbO2H,aq","TbO2-","TbHCO3+2","TbCl+2","TbCl2+","TbCl3,aq","TbCl4-","TbH2PO4+2","TbNO3+2","TbF+2","TbF2+","TbF3,aq","TbF4-","TbSO4+","DyCO3+","DyHCO3+2","DyCl+2","DyCl2+","DyCl3,aq","DyCl4-","DyH2PO4+2","DyNO3+2","DyF+2","DyF2+","DyF3,aq","DyF4-","DyOH+2","DyO+","DyO2H,aq","DyO2-","DySO4+","HoCO3+","HoHCO3+2","HoCl+2","HoCl2+","HoCl3,aq","HoCl4-","HoH2PO4+2","HoNO3+2","HoF+2","HoF2+","HoF3,aq","HoF4-","HoOH+2","HoO+","HoO2H,aq","HoO2-","HoSO4+","ErCO3+","ErHCO3+2","ErCl+2","ErCl2+","ErCl3,aq","ErCl4-","ErH2PO4+2","ErNO3+2","ErF+2","ErF2+","ErF3,aq","ErF4-","ErOH+2","ErO+","ErO2H,aq","ErO2-","ErSO4+","TmCO3+","TmHCO3+2","TmCl+2","TmCl2+","TmCl3,aq","TmCl4-","TmH2PO4+2","TmNO3+2","TmF+2","TmF2+","TmF3,aq","TmF4-","TmOH+2","TmO+","TmO2H,aq","TmO2-","TmSO4+","YbCO3+","YbOH+2","YbO+","YbO2H,aq","YbO2-","YbHCO3+2","YbCl+2","YbCl2+","YbCl3,aq","YbCl4-","YbH2PO4+2","YbNO3+2","YbF+2","YbF2+","YbF3,aq","YbF4-","YbSO4+","LuCO3+","LuOH+2","LuO+","LuO2H,aq","LuO2-","LuHCO3+2","LuCl+2","LuCl2+","LuCl3,aq","LuCl4-","LuH2PO4+2","LuNO3+2","LuF+2","LuF2+","LuF3,aq","LuF4-","LuSO4+","NaSO4-","MgSO4,aq","HCl,aq","MgCO3,aq","CaCO3,aq","SrCO3,aq","BaCO3,aq","BeCl+","BeCl2,aq","FeCl+2","CoCl+","CuCl,aq","CuCl2-","CuCl3-2","CuCl+","CuCl2,aq","CuCl3-","CuCl4-2","CdCl+","CdCl2,aq","CdCl3-","CdCl4-2","TlCl,aq","TlCl+2","AuCl,aq","AuCl2-","AuCl3-2","AuCl4-","HgCl+","HgCl2,aq","HgCl3-","HgCl4-2","InCl+2","BeF+","BeF2,aq","BeF3-","BeF4-2","MnF+","FeF+","FeF+2","CoF+","NiF+","CuF+","ZnF+","AgF,aq","CdF+","CdF2,aq","BaF+","TlF,aq","HgF+","InF+2","PbF+","PbF2,aq","Ag(HS)2-","Au(HS)2-","Pb(HS)2,aq","Pb(HS)3-","Mg(HSiO3)+","Ca(HSiO3)+","AlF+2","AlF2+","AlF3,aq","AlF4-","Al(OH)2F2-","Al(OH)2F,aq","Al(OH)F2,aq","AlSO4+","NaAl(OH)3F,aq","NaAl(OH)2F2,aq","CH4,aq","HAcetate,aq","Acetate-","1-BUTANAMINE,aq","1-BUTANOL,aq","1-BUTENE,aq","1-BUTYNE,aq","1-HEPTANAMINE,aq","1-HEPTANOL,aq","1-HEPTENE,aq","1-HEPTYNE,aq","1-HEXANAMINE,aq","1-HEXANOL,aq","1-HEXENE,aq","1-HEXYNE,aq","1-OCTANAMINE,aq","1-OCTANOL,aq","1-OCTENE,aq","1-OCTYNE,aq","1-PENTANAMINE,aq","1-PENTANOL,aq","1-PENTENE,aq","1-PENTYNE,aq","1-PROPANAMINE,aq","1-PROPANOL,aq","1-PROPENE,aq","1-PROPYNE,aq","2-BUTANONE,aq","2-HEPTANONE,aq","2-HEXANONE,aq","2-HYDROXYBUTANOATE","2-HYDROXYBUTANOIC,aq","2-HYDROXYDECANOATE","2-HYDROXYDECANOIC,aq","2-HYDROXYHEPTANOIC","2-HYDROXYHEXANOATE","2-HYDROXYHEXANOIC,aq","2-HYDROXYNONANOATE","2-HYDROXYNONANOIC,aq","2-HYDROXYOCTANOATE","2-HYDROXYOCTANOIC,aq","2-HYDROXYPENTANOIC","2-OCTANONE,aq","2-PENTANONE,aq","A-AMINOBUTYRIC,aq","ACETAMIDE,aq","ACETONE,aq","ADIPATE","ADIPIC-ACID,aq","ALANYLGLYCINE,aq","ASPARAGINE,aq","ASPARTIC-ACID,aq","AZELAIC-ACID,aq","AZELATE","BENZENE,aq","BENZOATE","BENZOIC-ACID,aq","o-CRESOL,aq","m-CRESOL,aq","p-CRESOL,aq","DECANOATE","DECANOIC-ACID,aq","2,3-DMP,aq","2,4-DMP,aq","2,5-DMP,aq","2,6-DMP,aq","3,4-DMP,aq","3,5-DMP,aq","DODECANOATE","DODECANOIC-ACID,aq","ETHANAMINE,aq","ETHANE,aq","ETHANOL,aq","ETHYLACETATE,aq","ETHYLBENZENE,aq","ETHYLENE,aq","ETHYNE,aq","GLUTAMIC-ACID,aq","GLUTAMINE,aq","GLUTARATE","GLUTARIC-ACID,aq","DIGLYCINE,aq","H-ADIPATE","H-AZELATE","H-GLUTARATE","H-MALONATE","H-OXALATE","H-PIMELATE","H-SEBACATE","H-SUBERATE","H-SUCCINATE","HEPTANOATE","HEPTANOIC-ACID,aq","HEXANOATE","HEXANOIC-ACID,aq","ISOLEUCINE,aq","LEUCINE,aq","m-TOLUATE","m-TOLUIC-ACID,aq","MALONATE","MALONIC-ACID,aq","METHANAMINE,aq","METHANOL,aq","METHIONINE,aq","N-BUTANE,aq","N-BUTYLBENZENE,aq","N-HEPTANE,aq","N-HEPTYLBENZENE,aq","N-HEXANE,aq","N-HEXYLBENZENE,aq","N-OCTANE,aq","N-OCTYLBENZENE,aq","N-PENTANE,aq","N-PENTYLBENZENE,aq","N-PROPYLBENZENE,aq","NONANOATE","NONANOIC-ACID,aq","o-TOLUATE","o-TOLUIC-ACID,aq","OCTANOATE","OCTANOIC-ACID,aq","OXALATE","OXALIC-ACID,aq","p-TOLUATE","p-TOLUIC-ACID,aq","PHENOL,aq","PHENYLALANINE,aq","PIMELATE","PIMELIC-ACID,aq","PROPANE,aq","SEBACATE","SEBACIC-ACID,aq","SERINE,aq","SUBERATE","SUBERIC-ACID,aq","SUCCINATE","SUCCINIC-ACID,aq","THREONINE,aq","TOLUENE,aq","TRYPTOPHAN,aq","TYROSINE,aq","UNDECANOATE","UNDECANOIC-ACID,aq","UREA,aq","VALINE,aq","ACETALDEHYDE,aq","BUTANAL,aq","DECANAL,aq","FORMALDEHYDE,aq","HEPTANAL,aq","HEXANAL,aq","NONANAL,aq","OCTANAL,aq","PENTANAL,aq","PROPANAL,aq","HCN,aq","CN-","OCN-","SCN-","SeCN-","Bi(Ac)+2","Bi(Ac)2+","Bi(Ac)3,aq","Dy(Ac)+2","Dy(Ac)2+","Dy(Ac)3,aq","Ho(Ac)+2","Ho(Ac)2+","Ho(Ac)3,aq","Er(Ac)+2","Er(Ac)2+","Er(Ac)3,aq","Tm(Ac)+2","Tm(Ac)2+","Tm(Ac)3,aq","Be(Ac)+","Be(Ac)2,aq","Ra(Ac)+","Ra(Ac)2,aq","Au(Ac),aq","Au(Ac)2-","Li(Ac),aq","Li(Ac)2-","Na(Ac),aq","Na(Ac)2-","K(Ac),aq","K(Ac)2-","Mg(Ac)+","Mg(Ac)2,aq","Sr(Ac)+","Sr(Ac)2,aq","Ba(Ac)+","Ba(Ac)2,aq","Cu(Ac),aq","Cu(Ac)2-","Rb(Ac),aq","Rb(Ac)2-","Tl(Ac),aq","Tl(Ac)2-","Cs(Ac),aq","Cs(Ac)2-","Pb(Ac)+","Pb(Ac)2,aq","Mn(Ac)+","Mn(Ac)2,aq","Mn(Ac)3-","Co(Ac)+","Co(Ac)2,aq","Co(Ac)3-","Ni(Ac)+","Ni(Ac)2,aq","Ni(Ac)3-","Cu(Ac)+","Cu(Ac)2,aq","Cu(Ac)3-","NH4(Ac),aq","NH4(Ac)2-","UO2(Ac)+","UO2(Ac)2,aq","UO2(Ac)3-","Ag(Ac),aq","Ag(Ac)2-","Cd(Ac)+","Cd(Ac)2,aq","Cd(Ac)3-","Hg(Ac)+","Hg(Ac)2,aq","Hg(Ac)3-","Sc(Ac)+2","Sc(Ac)2+","Sc(Ac)3,aq","U(Ac)+2","U(Ac)2+","U(Ac)3,aq","Pr(Ac)+2","Pr(Ac)2+","Pr(Ac)3,aq","La(Ac)+2","La(Ac)2+","La(Ac)3,aq","Nd(Ac)+2","Nd(Ac)2+","Nd(Ac)3,aq","Ce(Ac)+2","Ce(Ac)2+","Ce(Ac)3,aq","Gd(Ac)+2","Gd(Ac)2+","Gd(Ac)3,aq","Sm(Ac)+2","Sm(Ac)2+","Sm(Ac)3,aq","Yb(Ac)+2","Yb(Ac)2+","Yb(Ac)3,aq","Eu(Ac)+2","Eu(Ac)2+","Eu(Ac)3,aq","Y(Ac)+2","Y(Ac)2+","Y(Ac)3,aq","Lu(Ac)+2","Lu(Ac)2+","Lu(Ac)3,aq","Tb(Ac)+2","Tb(Ac)2+","Tb(Ac)3,aq","Pb(Ac)3-","Fe(Ac)+","Fe(Ac)2,aq","Zn(Ac)+","Zn(Ac)2,aq","Zn(Ac)3-","Ca(Ac)+","Ca(Ac)2,aq","Al(Ac)+2","Al(Ac)2+","Al(Ac)3,aq","LACTATE","LACTIC-ACID,aq","Li(Lac),aq","Mg(Lac)+","Mg(Lac)2,aq","Ca(Lac)+","Ca(Lac)2,aq","Sr(Lac)+","Sr(Lac)2,aq","Ba(Lac)+","Ba(Lac)2,aq","Mn(Lac)+","Mn(Lac)2,aq","Co(Lac)+","Co(Lac)2,aq","Ni(Lac)+","Ni(Lac)2,aq","Cu(Lac)+","Cu(Lac)2,aq","Zn(Lac)+","Zn(Lac)2,aq","Cd(Lac)+","Cd(Lac)2,aq","La(Lac)+2","Lu(Lac)+2","Na(Lac),aq","Na(Lac)2-","K(Lac),aq","K(Lac)2-","Pb(Lac)+","Pb(Lac)2,aq","Fe(Lac)+","Fe(Lac)2,aq","Eu(Lac)+","Eu(Lac)2,aq","GLYCOLIC-ACID,aq","GLYCOLATE","Li(Glyc),aq","Mg(Glyc)+","Mg(Glyc)2,aq","Ca(Glyc)+","Ca(Glyc)2,aq","Sr(Glyc)+","Sr(Glyc)2,aq","Ba(Glyc)+","Ba(Glyc)2,aq","Mn(Glyc)+","Mn(Glyc)2,aq","Co(Glyc)+","Co(Glyc)2,aq","Ni(Glyc)+","Ni(Glyc)2,aq","Cu(Glyc)+","Cu(Glyc)2,aq","Zn(Glyc)+","Zn(Glyc)2,aq","Cd(Glyc)+","Cd(Glyc)2,aq","Eu(Glyc)+","Eu(Glyc)2,aq","Na(Glyc),aq","Na(Glyc)2-","K(Glyc),aq","K(Glyc)2-","Pb(Glyc)+","Pb(Glyc)2,aq","Fe(Glyc)+","Fe(Glyc)2,aq","ALANINE,aq","ALANATE","Cd(Alan)+","Cd(Alan)2,aq","Ca(Alan)+","Ca(Alan)2,aq","Pb(Alan)+","Pb(Alan)2,aq","Mg(Alan)+","Mg(Alan)2,aq","Sr(Alan)+","Sr(Alan)2,aq","Mn(Alan)+","Mn(Alan)2,aq","Co(Alan)+","Co(Alan)2,aq","Ni(Alan)+","Ni(Alan)2,aq","Cu(Alan)+","Cu(Alan)2,aq","Zn(Alan)+","Zn(Alan)2,aq","Ba(Alan)+","Ba(Alan)2,aq","Fe(Alan)+","Fe(Alan)2,aq","Eu(Alan)+","Eu(Alan)2,aq","GLYCINE,aq","GLYCINATE","Cd(Gly)+","Cd(Gly)2,aq","Ca(Gly)+","Ca(Gly)2,aq","Sr(Gly)+","Sr(Gly)2,aq","Mn(Gly)+","Mn(Gly)2,aq","Fe(Gly)+","Fe(Gly)2,aq","Co(Gly)+","Co(Gly)2,aq","Pb(Gly)+","Pb(Gly)2,aq","Mg(Gly)+","Mg(Gly)2,aq","Ni(Gly)+","Ni(Gly)2,aq","Cu(Gly)+","Cu(Gly)2,aq","Zn(Gly)+","Zn(Gly)2,aq","Eu(Gly)+","Eu(Gly)2,aq","Ba(Gly)+","Ba(Gly)2,aq","FORMIC-ACID,aq","FORMATE","Mg(For)+","Mg(For)2,aq","Ca(For)+","Ca(For)2,aq","Sr(For)+","Sr(For)2,aq","Ba(For)+","Ba(For)2,aq","Cu(For)+","Cu(For)2,aq","Cd(For)+","Cd(For)2,aq","Na(For),aq","Na(For)2-","K(For),aq","K(For)2-","La(For)+2","La(For)2+","Eu(For)+","Eu(For)2,aq","U(For)+2","U(For)2+","Eu(For)+2","Eu(For)2+","Gd(For)+2","Gd(For)2+","Yb(For)+2","Yb(For)2+","Pb(For)+","Pb(For)2,aq","Mn(For)+","Mn(For)2,aq","Co(For)+","Co(For)2,aq","Ni(For)+","Ni(For)2,aq","Zn(For)+","Zn(For)2,aq","Fe(For)+","Fe(For)2,aq","PROPANOIC-ACID,aq","PROPANOATE","Mg(Prop)+","Mg(Prop)2,aq","Ca(Prop)+","Ca(Prop)2,aq","Sr(Prop)+","Sr(Prop)2,aq","Ba(Prop)+","Ba(Prop)2,aq","Eu(Prop)+","Eu(Prop)2,aq","Cu(Prop)+","Cu(Prop)2,aq","Cd(Prop)+","Cd(Prop)2,aq","Pb(Prop)+","Pb(Prop)2,aq","Na(Prop),aq","Na(Prop)2-","K(Prop),aq","K(Prop)2-","La(Prop)+2","La(Prop)2+","Eu(Prop)+2","Eu(Prop)2+","Gd(Prop)+2","Gd(Prop)2+","Yb(Prop)+2","Yb(Prop)2+","U(Prop)+2","U(Prop)2+","Co(Prop)+","Co(Prop)2,aq","Ni(Prop)+","Ni(Prop)2,aq","Zn(Prop)+","Zn(Prop)2,aq","Fe(Prop)+","Fe(Prop)2,aq","Mn(Prop)+","Mn(Prop)2,aq","BUTANOIC-ACID,aq","BUTANOATE","U(But)+2","U(But)2+","Eu(But)+","Eu(But)2,aq","Mg(But)+","Ca(But)+","Ca(But)2,aq","Sr(But)+","Sr(But)2,aq","Ba(But)+","Ba(But)2,aq","Cu(But)+","Cu(But)2,aq","Cd(But)+","Cd(But)2,aq","Na(But),aq","Na(But)2-","K(But),aq","K(But)2-","La(But)+2","La(But)2+","Eu(But)+2","Eu(But)2+","Gd(But)+2","Gd(But)2+","Yb(But)+2","Yb(But)2+","Pb(But)+","Pb(But)2,aq","Mn(But)+","Mn(But)2,aq","Ni(But)+","Ni(But)2,aq","Zn(But)+","Zn(But)2,aq","Fe(But)+","Fe(But)2,aq","Co(But)+","Co(But)2,aq","PENTANOIC-ACID,aq","PENTANOATE","Ca(Pent)+","Ca(Pent)2,aq","Sr(Pent)+","Sr(Pent)2,aq","Ba(Pent)+","Ba(Pent)2,aq","Cu(Pent)+","Cu(Pent)2,aq","Na(Pent),aq","Na(Pent)2-","K(Pent),aq","K(Pent)2-","La(Pent)+2","La(Pent)2+","Eu(Pent)+2","Eu(Pent)2+","U(Pent)+2","Eu(Pent)+","Gd(Pent)+2","Gd(Pent)2+","Yb(Pent)+2","Yb(Pent)2+","Mg(Pent)+","Mg(Pent)2,aq","Pb(Pent)+","Pb(Pent)2,aq","Co(Pent)+","Co(Pent)2,aq","Ni(Pent)+","Ni(Pent)2,aq","Zn(Pent)+","Zn(Pent)2,aq","Cd(Pent)+","Cd(Pent)2,aq","Fe(Pent)+","Fe(Pent)2,aq","Mn(Pent)+","Mn(Pent)2,aq","Mg(But)2,aq","H4SiO4,aq","H2O"]
    }

@login_required
def create_experiment(request, app_module_id):
    request.active_nav_item = 'dashboard'

    # User input files can be passed as query parameters
    # <input name>=<path/to/user_file>
    # and also as data product URIs
    # <input name>=<data product URI>
    app_interface = ApplicationModuleViewSet.as_view(
        {'get': 'application_interface'})(request, app_module_id=app_module_id)
    if app_interface.status_code != 200:
        raise Exception("Failed to load application module data: {}".format(
            app_interface.data['detail']))
    user_input_values = {}
    for app_input in app_interface.data['applicationInputs']:
        if (app_input['type'] ==
                DataType.URI and app_input['name'] in request.GET):
            user_file_value = request.GET[app_input['name']]
            try:
                user_file_url = urlparse(user_file_value)
                if user_file_url.scheme == 'airavata-dp':
                    dp_uri = user_file_value
                    try:
                        data_product = request.airavata_client.getDataProduct(
                            request.authz_token, dp_uri)
                        if user_storage_sdk.exists(request, data_product):
                            user_input_values[app_input['name']] = dp_uri
                    except Exception:
                        logger.exception(
                            f"Failed checking data product uri: {dp_uri}")
            except ValueError:
                logger.exception(f"Invalid user file value: {user_file_value}")
        elif (app_input['type'] == DataType.STRING and
              app_input['name'] in request.GET):
            name = app_input['name']
            user_input_values[name] = request.GET[name]
    context = {
        'bundle_name': 'create-experiment',
        'app_module_id': app_module_id,
        'user_input_values': json.dumps(user_input_values)
    }
    if 'experiment-data-dir' in request.GET:
        context['experiment_data_dir'] = request.GET['experiment-data-dir']

    # Run through context processors
    for processor in [species_list]:
        context.update(species_list(request))
    return render(request,
                  #   'django_airavata_workspace/create_experiment.html',
                  'django_airavata_workspace/supcrtbl2.html',
                  context)


@login_required
def edit_experiment(request, experiment_id):
    request.active_nav_item = 'experiments'

    return render(request,
                  #   'django_airavata_workspace/edit_experiment.html',
                  'django_airavata_workspace/supcrtbl2.html',
                  {'bundle_name': 'edit-experiment',
                   'experiment_id': experiment_id})


@login_required
def view_experiment(request, experiment_id):
    request.active_nav_item = 'experiments'

    launching = json.loads(request.GET.get('launching', 'false'))
    response = FullExperimentViewSet.as_view(
        {'get': 'retrieve'})(request, experiment_id=experiment_id)
    if response.status_code != 200:
        raise Exception("Failed to load experiment data: {}".format(
            response.data['detail']))
    full_experiment_json = JSONRenderer().render(response.data).decode('utf-8')

    return render(request, 'django_airavata_workspace/view_experiment.html', {
        'bundle_name': 'view-experiment',
        'full_experiment_data': full_experiment_json,
        'launching': json.dumps(launching),
    })


@login_required
def user_storage(request):
    request.active_nav_item = 'storage'
    return render(request, 'django_airavata_workspace/base.html', {
        'bundle_name': 'user-storage'
    })
