INSERT INTO public.tbl_estado_usuario (codigo, nombre, descripcion)
VALUES ('ACTIVO', 'Activo', 'Usuario activo en el sistema')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO public.tbl_rol (codigo, nombre, descripcion)
VALUES
  ('CLIENTE', 'Cliente', 'Usuario final que realiza reservas'),
  ('PROVEEDOR', 'Proveedor', 'Proveedor de servicios comerciales')
ON CONFLICT (codigo) DO NOTHING;
